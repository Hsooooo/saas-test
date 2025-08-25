package com.illunex.emsaasrestapi.chat.mapper;

import com.illunex.emsaasrestapi.chat.vo.ChatRoomVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ChatRoomMapper {
    Optional<ChatRoomVO> selectByIdx(Integer idx);
    void insertByChatRoomVO(ChatRoomVO vo);

    List<ChatRoomVO> selectByPartnershipMemberIdx(Integer partnershipMemberIdx);

    Optional<ChatRoomVO> selectByPartnershipMemberIdxAndChatRoomIdx(Integer partnershipMemberIdx, Integer chatRoomIdx);
}
