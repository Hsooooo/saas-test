package com.illunex.emsaasrestapi.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.illunex.emsaasrestapi.chat.util.OpenAiSseParser;
import com.illunex.emsaasrestapi.chat.vo.ChatHistoryVO;
import com.illunex.emsaasrestapi.common.CurrentMember;
import com.illunex.emsaasrestapi.common.code.EnumCode;
import com.illunex.emsaasrestapi.member.vo.MemberVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AiProxyController {

    @Value("${ai.url}") String aiGptBase;
    private final WebClient webClient;
    private final ChatService chatService;
    private final ObjectMapper objectMapper;

    @RequestMapping(value = "ai/gpt/**", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter proxy(HttpServletRequest request,
                            @CurrentMember MemberVO memberVO,
                            @RequestParam("partnershipMemberIdx") Integer partnershipMemberIdx,
                            @RequestParam("query") String query,
                            @RequestParam(value = "title", required = false) String title,
                            @RequestParam(value = "user_id", required = false, defaultValue = "hi") String userId,
                            @RequestParam(value = "chatRoomIdx", required = false) Integer roomIdx) {

        final SseEmitter emitter = new SseEmitter(0L);
        Integer chatRoomIdx = null;
        List<ChatHistoryVO> histories = (roomIdx == null) ? List.of() : chatService.getRecentHistories(roomIdx, 6);
        String historyString = toHistoryJsonString(histories);
        if (roomIdx == null) {
            chatRoomIdx = chatService.resolveChatRoom(partnershipMemberIdx, title);
            chatService.saveHistoryAsync(chatRoomIdx, EnumCode.ChatRoom.SenderType.USER.getCode(), query);
        } else {
            chatRoomIdx = roomIdx;
            // 기존 채팅방
            chatService.saveHistoryAsync(chatRoomIdx, EnumCode.ChatRoom.SenderType.USER.getCode(), query);
        }

        URI target = UriComponentsBuilder.fromHttpUrl(aiGptBase)
                .path("/v2/api/report-generate")
                .queryParam("user_id", userId)
                .queryParam("query", query)
                .encode(StandardCharsets.UTF_8).build().toUri();

        log.info("AI Proxy to {} from memberIdx: {}, partnershipMemberIdx: {}, chatRoomIdx: {}, body: {}",
                target, memberVO.getIdx(), partnershipMemberIdx, chatRoomIdx, historyString);
        var tee = new java.io.ByteArrayOutputStream(16 * 1024);

        final int chatRoomIdxFinal = chatRoomIdx;

        webClient.post()
                .uri(target)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .headers(h -> h.add("X-Accel-Buffering", "no"))
                .bodyValue(Map.of("history", historyString))
                .exchangeToFlux(clientRes -> {
                    HttpStatusCode sc = clientRes.statusCode();

                    if (sc.is2xxSuccessful()) {
                        // 성공: 바이트로 받아 tee에 쓰고, 그대로 SSE로 중계
                        return clientRes.bodyToFlux(org.springframework.core.io.buffer.DataBuffer.class)
                                .doOnNext(db -> {
                                    try {
                                        var bb = db.asByteBuffer().asReadOnlyBuffer();
                                        byte[] bytes = new byte[bb.remaining()];
                                        bb.get(bytes);
                                        tee.write(bytes);

                                        // 문자열로 변환해서 emitter로 전송
                                        String chunk = new String(bytes, StandardCharsets.UTF_8);
                                        emitter.send(SseEmitter.event().data(chunk));
                                    } catch (Exception e) {
                                        emitter.completeWithError(e);
                                    }
                                });
                    } else {
                        // 에러: 에러 바디까지 읽어서 tee에 쓰고, SSE error 이벤트로 전달
                        return clientRes.bodyToMono(String.class)
                                .defaultIfEmpty("Upstream " + sc.value() + " " + sc)
                                .flatMapMany(errBody -> {
                                    try {
                                        byte[] bytes = errBody.getBytes(StandardCharsets.UTF_8);
                                        tee.write(bytes);
                                        String payload = "event: error\ndata: " +
                                                errBody.replace("\n", " ") + "\n\n";
                                        emitter.send(payload);
                                    } catch (Exception e) {
                                        emitter.completeWithError(e);
                                    }
                                    emitter.complete();
                                    return reactor.core.publisher.Flux.empty();
                                });
                    }
                })
                .doOnError(e -> {
                    try {
                        String payload = "event: error\ndata: " + e.getMessage() + "\n\n";
                        tee.write(payload.getBytes(StandardCharsets.UTF_8));
                        emitter.send(payload);
                    } catch (Exception ignore) { }
                    emitter.completeWithError(e);
                })
                .doOnComplete(() -> {
                    // 정상 종료
                    saveAssistant(chatRoomIdxFinal, tee);
                    emitter.complete();
                })
                .subscribe();

        return emitter;
    }

    private void saveAssistant(int chatRoomIdx, java.io.ByteArrayOutputStream tee) {
        String all = tee.toString(java.nio.charset.StandardCharsets.UTF_8);
        String last = OpenAiSseParser.extractLastMessageFromSequence(all);
        chatService.saveHistoryAsync(
                chatRoomIdx,
                EnumCode.ChatRoom.SenderType.ASSISTANT.getCode(),
                (last == null || last.isBlank()) ? all : last
        );
    }

    private String toHistoryJsonString(List<ChatHistoryVO> histories) {
        if (histories == null || histories.isEmpty()) return "[]"; // 문자열이어야 하므로 빈 배열 문자열
        var list = histories.stream()
                .map(h -> java.util.Map.of(
                        h.getSenderType().equals(EnumCode.ChatRoom.SenderType.USER.getCode()) ? "u" : "a",
                        h.getMessage() == null ? "" : h.getMessage()
                ))
                .toList();
        try {
            return objectMapper.writeValueAsString(list); // 예: [{"u":"..."},{"a":"..."}]
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new RuntimeException("history serialize failed", e);
        }
    }
}