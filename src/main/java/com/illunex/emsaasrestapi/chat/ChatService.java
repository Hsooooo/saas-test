package com.illunex.emsaasrestapi.chat;

import com.illunex.emsaasrestapi.chat.mapper.ChatHistoryMapper;
import com.illunex.emsaasrestapi.chat.mapper.ChatRoomMapper;
import com.illunex.emsaasrestapi.chat.vo.ChatHistoryVO;
import com.illunex.emsaasrestapi.chat.vo.ChatRoomVO;
import com.illunex.emsaasrestapi.common.CustomException;
import com.illunex.emsaasrestapi.common.CustomPageRequest;
import com.illunex.emsaasrestapi.common.CustomResponse;
import com.illunex.emsaasrestapi.common.ErrorCode;
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

@Service
@RequiredArgsConstructor
public class ChatService {
    private final PartnershipMemberMapper partnershipMemberMapper;
    private final ChatRoomMapper chatRoomMapper;
    private final ChatHistoryMapper chatHistoryMapper;

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
        Integer totalCount = chatHistoryMapper.countAllByChatRoomIdx(chatRoomIdx);
        return CustomResponse.builder()
                .data(new PageImpl<>(historyList, pageable, totalCount))
                .build();
    }
}
