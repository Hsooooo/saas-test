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
    public ResponseEntity<SseEmitter> proxy(HttpServletRequest request,
                            @CurrentMember MemberVO memberVO,
                            @RequestParam("partnershipMemberIdx") Integer partnershipMemberIdx,
                            @RequestParam("query") String query,
                            @RequestParam(value = "title", required = false) String title,
                            @RequestParam(value = "user_id", required = false, defaultValue = "hi") String userId,
                            @RequestParam(value = "chatRoomIdx", required = false) Integer roomIdx) {

        final SseEmitter emitter = new SseEmitter(0L);

        // 1) 신규/기존 방 결정 + USER 메시지 저장
        final int chatRoomIdx = (roomIdx == null)
                ? chatService.resolveChatRoom(partnershipMemberIdx, title)
                : roomIdx;
        final boolean created = (roomIdx == null);
        chatService.saveHistoryAsync(chatRoomIdx, EnumCode.ChatRoom.SenderType.USER.getCode(), query);

        // 2) 프론트가 바로 쓸 수 있도록 'meta' 이벤트로 roomIdx 전송
        try {
            emitter.send(SseEmitter.event()
                    .name("meta")
                    .data(Map.of("chatRoomIdx", chatRoomIdx, "created", created)));
        } catch (Exception e) {
            emitter.completeWithError(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(emitter);
        }

        // 3) 업스트림 호출 (히스토리는 바디의 문자열 필드로 보낸다는 최신 합의)
        var histories = (roomIdx == null) ? List.<ChatHistoryVO>of()
                : chatService.getRecentHistories(chatRoomIdx, 6);
        String historyString = toHistoryJsonString(histories); // 예: [{"u":"..."},{"a":"..."}] 를 문자열로

        URI target = UriComponentsBuilder.fromHttpUrl(aiGptBase)
                .path("/v2/api/report-generate")
                .queryParam("user_id", userId)
                .queryParam("query", query)
                .encode(StandardCharsets.UTF_8)
                .build()
                .toUri();

        var tee = new java.io.ByteArrayOutputStream(16 * 1024);

        webClient.post()
                .uri(target)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .headers(h -> h.add("X-Accel-Buffering", "no"))
                .bodyValue(Map.of("history", historyString))
                .exchangeToFlux(clientRes -> {
                    var sc = clientRes.statusCode();
                    if (sc.is2xxSuccessful()) {
                        return clientRes.bodyToFlux(org.springframework.core.io.buffer.DataBuffer.class)
                                .doOnNext(db -> {
                                    try {
                                        var bb = db.asByteBuffer().asReadOnlyBuffer();
                                        byte[] bytes = new byte[bb.remaining()];
                                        bb.get(bytes);
                                        tee.write(bytes);
                                        emitter.send(SseEmitter.event().data(new String(bytes, StandardCharsets.UTF_8)));
                                    } catch (Exception e) { emitter.completeWithError(e); }
                                });
                    } else {
                        return clientRes.bodyToMono(String.class)
                                .defaultIfEmpty("Upstream " + sc.value())
                                .flatMapMany(err -> {
                                    try {
                                        tee.write(err.getBytes(StandardCharsets.UTF_8));
                                        emitter.send("event: error\ndata: " + err.replace("\n"," ") + "\n\n");
                                    } catch (Exception ignore) {}
                                    emitter.complete();
                                    return reactor.core.publisher.Flux.empty();
                                });
                    }
                })
                .doOnError(e -> {
                    try {
                        var payload = "event: error\ndata: " + e.getMessage() + "\n\n";
                        tee.write(payload.getBytes(StandardCharsets.UTF_8));
                        emitter.send(payload);
                    } catch (Exception ignore) {}
                    emitter.completeWithError(e);
                })
                .doOnComplete(() -> {
                    saveAssistant(chatRoomIdx, tee);
                    try {
                        emitter.send(SseEmitter.event().name("done").data("ok"));
                    } catch (Exception ignore) {}
                    emitter.complete();
                })
                .subscribe();

        // 4) 헤더로도 roomIdx 노출( fetch 로 헤더 읽을 때 유용 )
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Chat-Room-Idx", String.valueOf(chatRoomIdx));
        headers.add("Access-Control-Expose-Headers", "X-Chat-Room-Idx");

        return new ResponseEntity<>(emitter, headers, HttpStatus.OK);
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