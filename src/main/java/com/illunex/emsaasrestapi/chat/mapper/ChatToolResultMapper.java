package com.illunex.emsaasrestapi.chat.mapper;

import com.illunex.emsaasrestapi.chat.vo.ChatToolResultVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ChatToolResultMapper {
    void insertByChatToolResultVO(ChatToolResultVO chatToolResultVO);

    List<ChatToolResultVO> selectByChatHistoryIdxIn(List<Integer> chatHistoryIdxs);

    void updateHistoryIdxByIdxs(int historyIdx, List<Long> toolResultIds);
}
