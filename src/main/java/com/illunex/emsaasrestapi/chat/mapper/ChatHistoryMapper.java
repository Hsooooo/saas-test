package com.illunex.emsaasrestapi.chat.mapper;

import com.illunex.emsaasrestapi.chat.vo.ChatHistoryVO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Mapper
public interface ChatHistoryMapper {
    void insertByChatHistoryVO(ChatHistoryVO chatHistoryVO);

    List<ChatHistoryVO> selectByChatRoomIdx(Integer chatRoomIdx);

    List<ChatHistoryVO> selectByChatRoomIdxAndPageable(Integer chatRoomIdx, Pageable pageable);

    Integer countAllByChatRoomIdx(Integer chatRoomIdx);

    List<ChatHistoryVO> selectRecentByChatRoomIdx(Integer chatRoomIdx, int count);

    void updateMessageByIdx(ChatHistoryVO chatHistoryVO);
}
