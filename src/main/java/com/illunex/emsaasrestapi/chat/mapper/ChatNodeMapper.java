package com.illunex.emsaasrestapi.chat.mapper;

import com.illunex.emsaasrestapi.chat.vo.ChatNodeVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatNodeMapper {
    void insertBulkNode(List<ChatNodeVO> list);
    List<ChatNodeVO> selectByChatNetworkIdxIn(@Param("ids") List<Integer> networkIds);
}
