package com.illunex.emsaasrestapi.chat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.illunex.emsaasrestapi.chat.mapper.ChatToolResultMapper;
import com.illunex.emsaasrestapi.common.code.EnumCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ToolResultService {
    private final ChatService chatService;
    private final ChatToolResultMapper chatToolResultMapper;

    @Transactional
    /** tool payload upsert */
    public List<Long> upsertToolPayload(String payloadJson) throws JsonProcessingException {
        return chatService.insertChatTool(payloadJson);
    }

    @Transactional
    public void linkResultsToHistory(List<Long> toolResultIds, int historyIdx) {
        chatToolResultMapper.updateHistoryIdxByIdxs(historyIdx, toolResultIds);
    }
}