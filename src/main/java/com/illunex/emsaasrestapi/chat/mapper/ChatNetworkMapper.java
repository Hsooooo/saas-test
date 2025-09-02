package com.illunex.emsaasrestapi.chat.mapper;

import com.illunex.emsaasrestapi.chat.vo.ChatNetworkVO;
import com.illunex.emsaasrestapi.chat.vo.ChatToolResultVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ChatNetworkMapper {
    void insertByChatNetworkVO(ChatNetworkVO chatNetworkVO);
    List<ChatNetworkVO> selectByChatHistoryIdxIn(List<Integer> chatHistoryIdxList);
}
