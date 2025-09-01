package com.illunex.emsaasrestapi.chat.mapper;

import com.illunex.emsaasrestapi.chat.UpstreamSseClient;
import com.illunex.emsaasrestapi.chat.vo.ChatFileVO;
import com.illunex.emsaasrestapi.chat.vo.ChatToolResultVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ChatFileMapper {
    void insertByChatFileVO(ChatFileVO chatFileVO);

    List<ChatFileVO> selectByChatHistoryIdxIn(List<Integer> historyIdxs);
}
