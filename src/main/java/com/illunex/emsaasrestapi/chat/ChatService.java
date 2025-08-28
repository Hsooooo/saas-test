package com.illunex.emsaasrestapi.chat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.illunex.emsaasrestapi.chat.dto.ResponseChatDTO;
import com.illunex.emsaasrestapi.chat.mapper.ChatHistoryMapper;
import com.illunex.emsaasrestapi.chat.mapper.ChatRoomMapper;
import com.illunex.emsaasrestapi.chat.mapper.ChatToolResultMapper;
import com.illunex.emsaasrestapi.chat.util.OpenAiSseParser;
import com.illunex.emsaasrestapi.chat.vo.ChatHistoryVO;
import com.illunex.emsaasrestapi.chat.vo.ChatRoomVO;
import com.illunex.emsaasrestapi.chat.vo.ChatToolResultVO;
import com.illunex.emsaasrestapi.common.CustomException;
import com.illunex.emsaasrestapi.common.CustomPageRequest;
import com.illunex.emsaasrestapi.common.CustomResponse;
import com.illunex.emsaasrestapi.common.ErrorCode;
import com.illunex.emsaasrestapi.common.code.BaseCodeEnum;
import com.illunex.emsaasrestapi.common.code.EnumCode;
import com.illunex.emsaasrestapi.member.vo.MemberVO;
import com.illunex.emsaasrestapi.partnership.mapper.PartnershipMemberMapper;
import com.illunex.emsaasrestapi.partnership.vo.PartnershipMemberVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final PartnershipMemberMapper partnershipMemberMapper;
    private final ChatRoomMapper chatRoomMapper;
    private final ChatHistoryMapper chatHistoryMapper;
    private final ChatToolResultMapper chatToolResultMapper;
    private final ObjectMapper om;

    public int resolveChatRoom(int partnershipMemberIdx, String title) {
        ChatRoomVO room = new ChatRoomVO();
        room.setPartnershipMemberIdx(partnershipMemberIdx);
        room.setTitle(title != null ? title : "새 대화");
        chatRoomMapper.insertByChatRoomVO(room);
        return room.getIdx();
    }

    public void saveHistoryAsync(int chatRoomIdx, String senderType, String message) {
        Mono.fromRunnable(() -> {
            ChatHistoryVO h = new ChatHistoryVO();
            h.setChatRoomIdx(chatRoomIdx);
            h.setSenderType(senderType);
            h.setMessage(message);
            chatHistoryMapper.insertByChatHistoryVO(h);
        }).subscribeOn(Schedulers.boundedElastic()).subscribe();
    }

    public CustomResponse<?> getChatRoomList(MemberVO memberVO, Integer partnershipMemberIdx) throws CustomException {
        PartnershipMemberVO pm = partnershipMemberMapper.selectByIdx(partnershipMemberIdx)
                .orElseThrow(() -> new IllegalArgumentException("Partnership member not found"));
        if (!memberVO.getIdx().equals(pm.getMemberIdx())) {
            throw new CustomException(ErrorCode.COMMON_INVALID);
        }

        return CustomResponse.builder()
                .data(chatRoomMapper.selectByPartnershipMemberIdx(partnershipMemberIdx))
                .build();
    }

    public CustomResponse<?> getChatHistory(MemberVO memberVO, Integer partnershipMemberIdx, Integer chatRoomIdx, CustomPageRequest page, String[] sort) throws CustomException {
        PartnershipMemberVO pm = partnershipMemberMapper.selectByIdx(partnershipMemberIdx)
                .orElseThrow(() -> new IllegalArgumentException("Partnership member not found"));
        if (!memberVO.getIdx().equals(pm.getMemberIdx())) {
            throw new CustomException(ErrorCode.COMMON_INVALID);
        }
        chatRoomMapper.selectByPartnershipMemberIdxAndChatRoomIdx(partnershipMemberIdx, chatRoomIdx)
                .orElseThrow(() -> new IllegalArgumentException("No access to this chat room"));
        Pageable pageable = page.of(sort);

        List<ChatHistoryVO> historyList = chatHistoryMapper.selectByChatRoomIdxAndPageable(chatRoomIdx, pageable);
        List<ResponseChatDTO.History> response = historyList.stream().map(h -> {
            List<ChatToolResultVO> tools = chatToolResultMapper.selectByChatHistoryIdx(h.getIdx());
            List<ResponseChatDTO.ToolResult> toolResults = tools.stream().map(t -> ResponseChatDTO.ToolResult.builder()
                    .idx(t.getIdx())
                    .chatHistoryIdx(t.getChatHistoryIdx())
                    .toolType(t.getToolType())
                    .title(t.getTitle())
                    .url(t.getUrl())
                    .createDate(t.getCreateDate())
                    .updateDate(t.getUpdateDate())
                    .build()).toList();
            return ResponseChatDTO.History.builder()
                    .idx(h.getIdx())
                    .chatRoomIdx(h.getChatRoomIdx())
                    .message(h.getMessage())
                    .senderType(h.getSenderType())
                    .createDate(h.getCreateDate())
                    .updateDate(h.getUpdateDate())
                    .toolResults(toolResults)
                    .build();
        }).toList();

        Integer totalCount = chatHistoryMapper.countAllByChatRoomIdx(chatRoomIdx);
        return CustomResponse.builder()
                .data(new PageImpl<>(response, pageable, totalCount))
                .build();
    }

    public List<ChatHistoryVO> getRecentHistories(Integer chatRoomIdx, int count) {
        return chatHistoryMapper.selectRecentByChatRoomIdx(chatRoomIdx, count);
    }

    /** 어시스턴트/유저 메시지를 '동기' 저장하고 PK를 반환 */
    int saveHistoryAndReturnIdx(int chatRoomIdx, String senderTypeCode, String message) {
        ChatHistoryVO h = new ChatHistoryVO();
        h.setChatRoomIdx(chatRoomIdx);
        h.setSenderType(senderTypeCode);
        h.setMessage(message);
        chatHistoryMapper.insertByChatHistoryVO(h);
        return h.getIdx();
    };

    /** 기존 히스토리 본문 교체 (최종 응답으로 덮어쓰기) */
    void updateHistoryContent(int historyIdx, String content) {
        ChatHistoryVO h = new ChatHistoryVO();
        h.setIdx(historyIdx);
        h.setMessage(content);
        chatHistoryMapper.updateMessageByIdx(h);
    };

    public void insertChatTool(Integer historyIdx, String toolType, String payloadJson) throws JsonProcessingException {
        // Jackson으로 파싱
        JsonNode root = om.readTree(payloadJson);

        JsonNode resultsNode = root.path("results");

        if (resultsNode.isArray()) {
            for (JsonNode item : resultsNode) {
                String title = item.path("title").asText("");
                String url = item.path("url").asText("");
                ChatToolResultVO vo = new ChatToolResultVO();
                vo.setChatHistoryIdx(historyIdx);
                vo.setToolType(EnumCode.ChatToolResult.ToolType.QUERY_RESULT.getCode());
                vo.setTitle(title);
                vo.setUrl(url);
                chatToolResultMapper.insertByChatToolResultVO(vo);
            }
        }
    }

}
