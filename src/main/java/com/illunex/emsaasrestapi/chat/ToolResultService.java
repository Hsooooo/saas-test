package com.illunex.emsaasrestapi.chat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.illunex.emsaasrestapi.chat.mapper.ChatMcpMapper;
import com.illunex.emsaasrestapi.chat.mapper.ChatToolResultMapper;
import com.illunex.emsaasrestapi.chat.vo.ChatMcpVO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ToolResultService {
    private final ChatService chatService;
    private final ChatToolResultMapper chatToolResultMapper;
    private final ChatMcpMapper chatMcpMapper;

    @Transactional
    /** tool payload upsert */
    public List<Long> upsertToolPayload(String payloadJson) throws JsonProcessingException {
        return chatService.insertChatTool(payloadJson);
    }

    @Transactional
    public void linkResultsToHistory(List<Long> toolResultIds, int historyIdx) {
        chatToolResultMapper.updateHistoryIdxByIdxs(historyIdx, toolResultIds);
    }

    @Transactional
    public void insertChatMcpArray(Set<String> mcpNames, int historyIdx) {
        for (String name : mcpNames) {
            ChatMcpVO mcpVO = new ChatMcpVO();
            mcpVO.setName(name);
            mcpVO.setChatHistoryIdx(historyIdx);
            chatMcpMapper.insertByChatMcpVO(mcpVO);
        }
    }
}