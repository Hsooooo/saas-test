package com.illunex.emsaasrestapi.chat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.illunex.emsaasrestapi.chat.dto.ResponseChatDTO;
import com.illunex.emsaasrestapi.chat.mapper.*;
import com.illunex.emsaasrestapi.chat.util.OpenAiSseParser;
import com.illunex.emsaasrestapi.chat.vo.*;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final PartnershipMemberMapper partnershipMemberMapper;
    private final ChatRoomMapper chatRoomMapper;
    private final ChatHistoryMapper chatHistoryMapper;
    private final ChatToolResultMapper chatToolResultMapper;
    private final ChatFileMapper chatFileMapper;
    private final ChatFileSlideMapper chatFileSlideMapper;
    private final ObjectMapper om;

    public int resolveChatRoom(int partnershipMemberIdx, String title) {
        ChatRoomVO room = new ChatRoomVO();
        room.setPartnershipMemberIdx(partnershipMemberIdx);
        room.setTitle(title != null ? title : "새 대화");
        chatRoomMapper.insertByChatRoomVO(room);
        return room.getIdx();
    }

    public void saveHistoryAsync(int chatRoomIdx, String senderType, String categoryType, String message) {
        Mono.fromRunnable(() -> {
            ChatHistoryVO h = new ChatHistoryVO();
            h.setChatRoomIdx(chatRoomIdx);
            h.setSenderType(senderType);
            h.setMessage(message);
            h.setCategoryType(categoryType);
            chatHistoryMapper.insertByChatHistoryVO(h);
        }).subscribeOn(Schedulers.boundedElastic()).subscribe();
    }

    public CustomResponse<?> getChatRoomList(MemberVO memberVO, Integer partnershipMemberIdx, CustomPageRequest page, String[] sort) throws CustomException {
        PartnershipMemberVO pm = partnershipMemberMapper.selectByIdx(partnershipMemberIdx)
                .orElseThrow(() -> new IllegalArgumentException("Partnership member not found"));
        if (!memberVO.getIdx().equals(pm.getMemberIdx())) {
            throw new CustomException(ErrorCode.COMMON_INVALID);
        }
        Pageable pageable = page.of(sort);
        return CustomResponse.builder()
                .data(chatRoomMapper.selectByPartnershipMemberIdx(partnershipMemberIdx))
                .build();
    }

    public CustomResponse<?> getChatHistory(
            MemberVO memberVO, Integer partnershipMemberIdx, Integer chatRoomIdx,
            CustomPageRequest page, String[] sort) throws CustomException {

        var pm = partnershipMemberMapper.selectByIdx(partnershipMemberIdx)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMON_INVALID)); // not found도 통일
        if (!Objects.equals(memberVO.getIdx(), pm.getMemberIdx())) {
            throw new CustomException(ErrorCode.COMMON_INVALID);
        }

        chatRoomMapper.selectByPartnershipMemberIdxAndChatRoomIdx(partnershipMemberIdx, chatRoomIdx)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMON_INVALID)); // 메시지/코드 통일

        Pageable pageable = page.ofWithStableSort(sort, List.of("create_date,DESC", "idx,DESC")); // 타이브레이커 강제

        List<ChatHistoryVO> historyList =
                chatHistoryMapper.selectByChatRoomIdxAndPageable(chatRoomIdx, pageable);

        if (historyList.isEmpty()) {
            return CustomResponse.builder()
                    .data(new PageImpl<>(List.of(), pageable, 0))
                    .build();
        }

        // === 1) 배치 조회 준비
        List<Integer> historyIds = historyList.stream().map(ChatHistoryVO::getIdx).toList();

        // === 2) 도구결과 배치 조회 후 groupBy
        Map<Integer, List<ChatToolResultVO>> toolMap =
                chatToolResultMapper.selectByChatHistoryIdxIn(historyIds).stream()
                        .collect(Collectors.groupingBy(ChatToolResultVO::getChatHistoryIdx));

        // === 3) 파일 배치 조회 → 슬라이드도 한 번에
        List<ChatFileVO> allFiles = chatFileMapper.selectByChatHistoryIdxIn(historyIds);
        Map<Integer, List<ChatFileVO>> fileMap =
                allFiles.stream().collect(Collectors.groupingBy(ChatFileVO::getChatHistoryIdx));

        // PPTX 파일들의 id만 뽑아서 슬라이드 배치 조회
        List<Long> pptxFileIds = allFiles.stream()
                .filter(f -> Objects.equals(f.getFileCd(), EnumCode.ChatFile.FileCd.PPTX.getCode()))
                .map(ChatFileVO::getIdx)
                .toList();

        Map<Long, List<ChatFileSlideVO>> slideMap = pptxFileIds.isEmpty()
                ? Map.of()
                : chatFileSlideMapper.selectByChatFileIdxIn(pptxFileIds).stream()
                .collect(Collectors.groupingBy(ChatFileSlideVO::getChatFileIdx));

        // === 4) DTO 매핑
        List<ResponseChatDTO.History> response = historyList.stream().map(h -> {
            // tools
            List<ResponseChatDTO.ToolResult> toolResults = toolMap.getOrDefault(h.getIdx(), List.of())
                    .stream()
                    .map(t -> ResponseChatDTO.ToolResult.builder()
                            .idx(t.getIdx())
                            .chatHistoryIdx(t.getChatHistoryIdx())
                            .toolType(t.getToolType())
                            .title(t.getTitle())
                            .url(t.getUrl())
                            .createDate(t.getCreateDate())
                            .updateDate(t.getUpdateDate())
                            .build())
                    .toList();

            // files (+slides)
            List<ResponseChatDTO.ChatFileResult> chatFiles = fileMap.getOrDefault(h.getIdx(), List.of())
                    .stream()
                    .map(f -> {
                        List<String> slides = slideMap.getOrDefault(f.getIdx(), List.of())
                                .stream()
                                .filter(Objects::nonNull)
                                .map(s -> s.getPage() + ":" + s.getContent())
                                .toList();
                        return ResponseChatDTO.ChatFileResult.builder()
                                .idx(f.getIdx())
                                .chatHistoryIdx(f.getChatHistoryIdx())
                                .fileName(f.getFileName())
                                .fileUrl(f.getFileUrl())
                                .filePath(f.getFilePath())
                                .fileSize(f.getFileSize())
                                .fileCd(f.getFileCd())
                                .slides(slides) // 빌더에서 바로
                                .createDate(f.getCreateDate())
                                .updateDate(f.getUpdateDate())
                                .build();
                    })
                    .toList();

            return ResponseChatDTO.History.builder()
                    .idx(h.getIdx())
                    .chatRoomIdx(h.getChatRoomIdx())
                    .message(h.getMessage())
                    .senderType(h.getSenderType())
                    .categoryType(h.getCategoryType())
                    .categoryTypeDesc(
                            h.getCategoryType() == null ? null : EnumCode.getCodeDesc(h.getCategoryType())
                    )
                    .createDate(h.getCreateDate())
                    .updateDate(h.getUpdateDate())
                    .toolResults(toolResults)
                    .chatFiles(chatFiles) // ← 누락 보완
                    .build();
        }).toList();

        int totalCount = chatHistoryMapper.countAllByChatRoomIdx(chatRoomIdx);

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
    void updateHistoryContent(int historyIdx, String content, String category) {
        ChatHistoryVO h = new ChatHistoryVO();
        h.setIdx(historyIdx);
        h.setMessage(content);
        h.setCategoryType(category);
        chatHistoryMapper.updateMessageAndCategoryTypeByIdx(h);
    };

    public List<Long> insertChatTool(String payloadJson) throws JsonProcessingException {
        List<Long> insertedIds = new ArrayList<>();
        // Jackson으로 파싱
        JsonNode root = om.readTree(payloadJson);

        JsonNode resultsNode = root.path("results");


        if (resultsNode.isArray()) {
            for (JsonNode item : resultsNode) {
                String title = item.path("title").asText("");
                String url = item.path("url").asText("");
                ChatToolResultVO vo = new ChatToolResultVO();
                vo.setToolType(EnumCode.ChatToolResult.ToolType.QUERY_RESULT.getCode());
                vo.setTitle(title);
                vo.setUrl(url);
                chatToolResultMapper.insertByChatToolResultVO(vo);
                Long newId = vo.getIdx();
                if (newId != null) {
                    insertedIds.add(newId);
                }
            }
        }

        return insertedIds;
    }

    int saveHistory(int chatRoomIdx, String senderCode, String categoryCode, String message) {
        ChatHistoryVO h = new ChatHistoryVO();
        h.setChatRoomIdx(chatRoomIdx);
        h.setSenderType(senderCode);
        h.setCategoryType(categoryCode);
        h.setMessage(message);
        chatHistoryMapper.insertByChatHistoryVO(h);
        return h.getIdx();
    };

}
