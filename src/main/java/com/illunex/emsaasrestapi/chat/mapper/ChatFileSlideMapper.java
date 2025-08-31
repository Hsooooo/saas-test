package com.illunex.emsaasrestapi.chat.mapper;

import com.illunex.emsaasrestapi.chat.UpstreamSseClient;
import com.illunex.emsaasrestapi.chat.vo.ChatFileSlideVO;
import com.illunex.emsaasrestapi.chat.vo.ChatFileVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ChatFileSlideMapper {
    void insertByChatFileSlideVO(ChatFileSlideVO chatFileSlideVO);

    List<ChatFileSlideVO> selectByChatFileIdxIn(List<Long> chatFileIdxs);
}
