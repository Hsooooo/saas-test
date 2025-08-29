package com.illunex.emsaasrestapi.chat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.illunex.emsaasrestapi.chat.util.OpenAiSseParser;
import com.illunex.emsaasrestapi.chat.vo.ChatHistoryVO;
import com.illunex.emsaasrestapi.common.CurrentMember;
import com.illunex.emsaasrestapi.common.code.BaseCodeEnum;
import com.illunex.emsaasrestapi.common.code.EnumCode;
import com.illunex.emsaasrestapi.member.vo.MemberVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AiProxyController {
    @Value("${ai.url}") String aiGptBase;
    private final UpstreamSseClient upstream;
    private final SseEventParser parser;
    private final HybridSseParser hybridParser;
    private final ToolResultService toolSvc;
    private final ChatService chatService;
    private final ObjectMapper om;

    @RequestMapping(value = "ai/gpt/v2/api/report-generate", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> proxy(@CurrentMember MemberVO memberVO,
                                            @RequestParam("partnershipMemberIdx") Integer pmIdx,
                                            @RequestParam(value = "title", required = false) String title,
                                            @RequestParam(value = "chatRoomIdx", required = false) Integer roomIdx,
                                            @RequestBody Map<String, String> body) {
        String query = (body.get("query") + "").trim();
        if (query.isBlank()) return ResponseEntity.badRequest().build();

        int chatRoomIdx = (roomIdx == null) ? chatService.resolveChatRoom(pmIdx, title) : roomIdx;
        chatService.saveHistoryAsync(chatRoomIdx, EnumCode.ChatRoom.SenderType.USER.getCode(), EnumCode.ChatHistory.CategoryType.USER.getCode(),query);

        SseEmitter emitter = new SseEmitter(0L);
        try { emitter.send(SseEmitter.event().name("meta").data(Map.of("chatRoomIdx", chatRoomIdx, "created", roomIdx == null))); } catch (Exception ignore) {}

        // upstream 본문 준비
        String historyString = toHistoryJsonString(roomIdx == null ? List.of() : chatService.getRecentHistories(chatRoomIdx, 6));
        Map<String, Object> payload = Map.of("query", query, "history", historyString);

        // 상태
        final var tee = new java.io.ByteArrayOutputStream(16 * 1024);
        final var tempAssistantIdx = new java.util.concurrent.atomic.AtomicReference<Integer>(null);

        // 스트림
        Flux<String> upstreamFlux = upstream.stream(aiGptBase, "/v2/api/report-generate", payload);
        var source = upstreamFlux.publish();
        source.connect();

        Disposable d1 = source.subscribe(chunk -> {
            try {
                tee.write(chunk.getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try { emitter.send(SseEmitter.event().data(chunk)); } catch (Exception ignore) {}
        }, e -> {
            try { emitter.send("event: error\ndata: " + (e.getMessage()==null?"":e.getMessage()) + "\n\n"); } catch (Exception ignore) {}
            emitter.complete();
        });

        Disposable d2 = hybridParser.parse(source).subscribe(ev -> {
            if (ev instanceof SseEvent.ToolProgress tp) {
                int idx = toolSvc.ensureTempAssistant(tempAssistantIdx.get(), chatRoomIdx);
                tempAssistantIdx.compareAndSet(null, idx);
                try {
                    toolSvc.upsertToolPayload(idx, tp.toolType(), tp.payloadJson());
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        }, e -> { /* 로그만 */ });

        Disposable d3 = source.ignoreElements().subscribe(null, null, () -> {
            String all = tee.toString(StandardCharsets.UTF_8);
            String last = OpenAiSseParser.extractLastMessageFromSequence(all);
            String category = OpenAiSseParser.extractLastCategoryFromSequence(all);
            String cateType = EnumCode.ChatHistory.CategoryType.getCodeByValue(category);
            toolSvc.finalizeAssistant(chatRoomIdx, tempAssistantIdx.get(), cateType, (last == null || last.isBlank()) ? all : last);
            try { emitter.send(SseEmitter.event().name("done").data("ok")); } catch (Exception ignore) {}
            emitter.complete();
        });

        emitter.onCompletion(() -> { d1.dispose(); d2.dispose(); d3.dispose(); });
        emitter.onTimeout(() -> { d1.dispose(); d2.dispose(); d3.dispose(); });
        emitter.onError(ex -> { d1.dispose(); d2.dispose(); d3.dispose(); });


        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Chat-Room-Idx", String.valueOf(chatRoomIdx));
        headers.add("Access-Control-Expose-Headers", "X-Chat-Room-Idx");
        return new ResponseEntity<>(emitter, headers, HttpStatus.OK);
    }

    private String toHistoryJsonString(List<ChatHistoryVO> histories) {
        if (histories == null || histories.isEmpty()) return "[]";
        try {
            var list = histories.stream()
                    .map(h -> Map.of(h.getSenderType().equals(EnumCode.ChatRoom.SenderType.USER.getCode()) ? "u" : "a",
                            h.getMessage() == null ? "" : h.getMessage()))
                    .toList();
            return om.writeValueAsString(list);
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}