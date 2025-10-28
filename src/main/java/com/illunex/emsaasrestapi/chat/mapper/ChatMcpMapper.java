package com.illunex.emsaasrestapi.chat.mapper;

import com.illunex.emsaasrestapi.chat.vo.ChatMcpVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ChatMcpMapper {
    void insertByChatMcpVO(ChatMcpVO chatMcpVO);
    List<ChatMcpVO> selectByChatHistoryIdx(Integer chatHistoryIdx);
}
