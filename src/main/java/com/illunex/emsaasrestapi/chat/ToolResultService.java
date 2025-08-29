package com.illunex.emsaasrestapi.chat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.illunex.emsaasrestapi.chat.mapper.ChatToolResultMapper;
import com.illunex.emsaasrestapi.common.code.EnumCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ToolResultService {
    private final ChatService chatService;
    private final ChatToolResultMapper chatToolResultMapper;

    /** 외부데이터 첫 감지 시 임시 assistant 생성 (idx 반환) */
    public int ensureTempAssistant(Integer currentIdx, int chatRoomIdx) {
        if (currentIdx != null) return currentIdx;
        return chatService.saveHistoryAndReturnIdx(
                chatRoomIdx,
                EnumCode.ChatRoom.SenderType.ASSISTANT.getCode(),
                "🔎 외부 데이터 수집 중…"   // 프리뷰
        );
    }

    /** tool payload upsert */
    public List<Long> upsertToolPayload(String payloadJson) throws JsonProcessingException {
        return chatService.insertChatTool(payloadJson);
    }

    /** 스트림 종료 시 최종 답변 저장(or 업데이트) */
    public void finalizeAssistant(int chatRoomIdx, Integer tempIdxOrNull, String category, String finalText) {
        if (tempIdxOrNull != null) {
            chatService.updateHistoryContent(tempIdxOrNull, finalText, category);
        } else {
            chatService.saveHistoryAsync(
                    chatRoomIdx,
                    EnumCode.ChatRoom.SenderType.ASSISTANT.getCode(),
                    category,
                    finalText
            );
        }
    }

    public void linkResultsToHistory(List<Long> toolResultIds, int historyIdx) {
        chatToolResultMapper.updateHistoryIdxByIdxs(historyIdx, toolResultIds);
    }
}