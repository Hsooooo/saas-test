package com.illunex.emsaasrestapi.chat;

import com.illunex.emsaasrestapi.common.CurrentMember;
import com.illunex.emsaasrestapi.common.CustomException;
import com.illunex.emsaasrestapi.common.CustomPageRequest;
import com.illunex.emsaasrestapi.common.CustomResponse;
import com.illunex.emsaasrestapi.member.vo.MemberVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
@Slf4j
public class ChatController {
    private final ChatService chatService;

    /**
     * 채팅방 목록 조회
     * @param memberVO
     * @param partnershipMemberIdx
     * @return
     */
    @GetMapping("/rooms")
    public CustomResponse<?> getChatRoomList(@CurrentMember MemberVO memberVO,
                                             @RequestParam Integer partnershipMemberIdx) throws CustomException {
        return chatService.getChatRoomList(memberVO, partnershipMemberIdx);
    }

    @GetMapping("/history")
    public CustomResponse<?> getChatHistory(@CurrentMember MemberVO memberVO,
                                            @RequestParam Integer partnershipMemberIdx,
                                            @RequestParam Integer chatRoomIdx,
                                            CustomPageRequest page,
                                            String[] sort) throws CustomException {
        return chatService.getChatHistory(memberVO,partnershipMemberIdx, chatRoomIdx, page, sort);
    }
}
