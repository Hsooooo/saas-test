package com.illunex.emsaasrestapi.chat.mapper;

import com.illunex.emsaasrestapi.chat.vo.ChatLinkVO;
import com.illunex.emsaasrestapi.chat.vo.ChatNetworkVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatLinkMapper {
    void insertBulkLink(List<ChatLinkVO> list);
    List<ChatLinkVO> selectByChatNetworkIdxIn(@Param("ids") List<Integer> networkIds);
}
