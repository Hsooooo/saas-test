package com.illunex.emsaasrestapi.chat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.illunex.emsaasrestapi.chat.util.OpenAiSseParser;
import com.illunex.emsaasrestapi.chat.vo.ChatHistoryVO;
import com.illunex.emsaasrestapi.common.CurrentMember;
import com.illunex.emsaasrestapi.common.code.EnumCode;
import com.illunex.emsaasrestapi.member.vo.MemberVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AiProxyController {

    @Value("${ai.url}") String aiGptBase;

    private final UpstreamSseClient upstream;     // aiGptBase 로 SSE 프록시하는 클라이언트 (Flux<String> data 청크)
    private final ChatService chatService;        // 채팅방/히스토리 저장
    private final ToolResultService toolSvc;      // tool_result upsert & link(history_idx)
    private final ObjectMapper om;

    @PostMapping(value = "ai/gpt/v2/api/report-generate", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> proxy(@CurrentMember MemberVO memberVO,
                                            @RequestParam("partnershipMemberIdx") Integer pmIdx,
                                            @RequestParam(value = "title", required = false) String title,
                                            @RequestParam(value = "chatRoomIdx", required = false) Integer roomIdx,
                                            @RequestBody Map<String, String> body) {

        final String raw = String.valueOf(body.get("query"));
        final String query = raw == null ? "" : raw.trim();
        if (query.isBlank()) return ResponseEntity.badRequest().build();

        final int chatRoomIdx = (roomIdx == null)
                ? chatService.resolveChatRoom(pmIdx, title)
                : roomIdx;

        // 1) USER 메시지 즉시 저장 (비동기여도 ok)
        chatService.saveHistoryAsync(
                chatRoomIdx,
                EnumCode.ChatRoom.SenderType.USER.getCode(),
                EnumCode.ChatHistory.CategoryType.USER.getCode(),
                query
        );

        // 2) 클라이언트에게 meta 이벤트로 방 정보 먼저 쏨
        SseEmitter emitter = new SseEmitter(0L);
        try {
            emitter.send(SseEmitter.event().name("meta").data(Map.of(
                    "chatRoomIdx", chatRoomIdx,
                    "created", roomIdx == null
            )));
        } catch (Exception ignore) {}

        // 3) 업스트림 페이로드 구성 (최근 히스토리 전달)
        final String historyString = toHistoryJsonString(
                roomIdx == null ? List.of() : chatService.getRecentHistories(chatRoomIdx, 6)
        );
        final Map<String, Object> payload = Map.of(
                "query", query,
                "history", historyString
        );

        // 4) 스트림 상태 변수
        final var tee = new java.io.ByteArrayOutputStream(32 * 1024);
        final var lastAssistant = new AtomicReference<String>("");
        final var lastCategory  = new AtomicReference<String>(null);
        final var lastPptxFlag  = new AtomicReference<String>(null);
        final var lastDocsFlag  = new AtomicReference<String>(null);

        // 툴 결과 즉시 upsert된 row들의 id 모음 → 완료 시 history_idx로 연결
        final List<Long> toolResultIds = new CopyOnWriteArrayList<>();

        // 업스트림 구독 (SSE 텍스트 조각을 그대로 흘려보내며, 동시에 파싱)
        Flux<String> stream = upstream.stream(aiGptBase, "/v2/api/report-generate", payload).share();

        // 4-1) 다운스트림으로 프록시 + tee 에 기록
        Disposable dProxy = stream.subscribe(chunk -> {
            try {
                tee.write(chunk.getBytes(StandardCharsets.UTF_8));
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            // 그대로 클라이언트에게 흘려보냄
            try { emitter.send(SseEmitter.event().data(chunk)); } catch (Exception ignore) {}
        }, e -> {
            try { emitter.send("event: error\ndata: " + (e.getMessage() == null ? "" : e.getMessage()) + "\n\n"); } catch (Exception ignore) {}
            emitter.complete();
        });

        // 4-2) 즉시 처리 파이프: “tool” 필드 포함된 이벤트 → tool_result upsert
        Disposable dTool = stream.subscribe(chunk -> {
            Optional<JsonNode> opt = tryExtractJsonFromSseChunk(chunk, om);
            if (opt.isEmpty()) return;

            JsonNode n = opt.get();

            // a) tool 이벤트 즉시 저장
            if (n.hasNonNull("tool")) {
                try {
                    List<Long> ids = toolSvc.upsertToolPayload(n.toString()); // 구현: payload JSON 기준 upsert → id 리스트 반환
                    if (ids != null && !ids.isEmpty()) toolResultIds.addAll(ids);
                } catch (JsonProcessingException e) {
                    log.warn("tool payload parse fail", e);
                }
            }

            // b) 마지막 assistant content 후보 누적(수신 모델 포맷에 맞춰 유연 처리)
            //    - delta/content/text 등 다양성 고려
            String content = extractAssistantText(n);
            if (content != null && !content.isBlank()) lastAssistant.set(content);

            // c) 분류/플래그류 스누핑
            if (n.has("category") && !n.get("category").isNull()) lastCategory.set(n.get("category").asText());
            if (n.has("pptx")     && !n.get("pptx").isNull())     lastPptxFlag.set(n.get("pptx").asText());
            if (n.has("docs")     && !n.get("docs").isNull())     lastDocsFlag.set(n.get("docs").asText());

        }, e -> log.warn("tool/assistant parser error: {}", e.getMessage()));

        // 4-3) 완료 시점: 마지막 메시지 → chat_history 저장, 그 history_idx로 tool_result들 연결
        Disposable dDone = stream.ignoreElements().subscribe(null, null, () -> {
            final String all = tee.toString(StandardCharsets.UTF_8);
            // 업스트림이 OpenAI 스타일 SSE 시퀀스를 준다면 백업 파서도 한번 더 시도
            final String fallbackLast = OpenAiSseParser.extractLastMessageFromSequence(all);
            final String finalText = (lastAssistant.get() == null || lastAssistant.get().isBlank())
                    ? (fallbackLast == null ? "" : fallbackLast)
                    : lastAssistant.get();

            // 카테고리 코드 변환
            String cate = lastCategory.get();
            final String cateCode = EnumCode.ChatHistory.CategoryType.getCodeByValue(cate);

            // PPTX/Docs 플래그는 필요 시 후처리
            final String pptx = lastPptxFlag.get();
            final String docs = lastDocsFlag.get();
            if ("yes".equalsIgnoreCase(pptx)) {
                // TODO: finalText로 PPTX 생성 및 링크 수집(필요하면)
                // callPptxGenerate(finalText);
            }
            if ("yes".equalsIgnoreCase(docs)) {
                // TODO: 문서 생성 플로우가 있으면 여기에
            }

            // 1) 어시스턴트 최종 메시지 저장 → history_idx 획득
            int historyIdx = chatService.saveHistory(
                    chatRoomIdx,
                    EnumCode.ChatRoom.SenderType.ASSISTANT.getCode(),
                    cateCode,
                    finalText
            );

            // 2) 방금 upsert했던 tool_result 들에 history_idx 연결
            if (!toolResultIds.isEmpty()) {
                try {
                    toolSvc.linkResultsToHistory(toolResultIds, historyIdx);
                } catch (Exception e) {
                    log.error("linkResultsToHistory failed", e);
                }
            }

            try { emitter.send(SseEmitter.event().name("done").data("ok")); } catch (Exception ignore) {}
            emitter.complete();
        });

        // 5) 리소스 정리
        Runnable cleanup = () -> {
            try { dProxy.dispose(); } catch (Exception ignore) {}
            try { dTool.dispose(); }  catch (Exception ignore) {}
            try { dDone.dispose(); }  catch (Exception ignore) {}
        };
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(ex -> cleanup.run());

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Chat-Room-Idx", String.valueOf(chatRoomIdx));
        headers.add("Access-Control-Expose-Headers", "X-Chat-Room-Idx");
        return new ResponseEntity<>(emitter, headers, HttpStatus.OK);
    }

    // 최근 히스토리를 프롬프트 압축 포맷으로 변환(예: [{"u":"..."},{"a":"..."}])
    private String toHistoryJsonString(List<ChatHistoryVO> histories) {
        if (histories == null || histories.isEmpty()) return "[]";
        try {
            var list = histories.stream()
                    .map(h -> Map.of(
                            h.getSenderType().equals(EnumCode.ChatRoom.SenderType.USER.getCode()) ? "u" : "a",
                            h.getMessage() == null ? "" : h.getMessage()
                    )).toList();
            return om.writeValueAsString(list);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // SSE 청크에서 JSON 노드 시도 추출 (data: {...}\n\n 형태나 그냥 {...} 를 모두 수용)
    private static Optional<JsonNode> tryExtractJsonFromSseChunk(String chunk, ObjectMapper om) {
        try {
            String s = chunk.trim();
            // "data:" 접두가 여러 줄일 수 있음 → 마지막 data 라인만 노림
            int idx = s.lastIndexOf("data:");
            if (idx >= 0) {
                s = s.substring(idx + 5).trim();
            }
            if (s.startsWith("{") && s.endsWith("}")) {
                return Optional.of(om.readTree(s));
            }
        } catch (Exception ignore) {}
        return Optional.empty();
    }

    // 다양한 공급자 포맷에서 어시스턴트 텍스트 후보를 뽑아냄
    private static String extractAssistantText(JsonNode n) {
        if (n.hasNonNull("content")) return n.get("content").asText();
        if (n.has("delta") && n.get("delta").hasNonNull("content")) return n.get("delta").get("content").asText();
        if (n.has("message") && n.get("message").hasNonNull("content")) return n.get("message").get("content").asText();
        if (n.hasNonNull("text")) return n.get("text").asText();
        return null;
    }

    // 필요 시 PPTX 생성 콜(엔드포인트 통일)
    private String callPptxGenerate(String mdText) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(aiGptBase).path("/v2/api/generate-pptx").toUriString();
            Map<String, String> request = Map.of("md_text", mdText);
            // WebClient는 외부 주입/공유 인스턴스로 바꿔도 됨
            String resp = org.springframework.web.reactive.function.client.WebClient.create(url)
                    .post().bodyValue(request).retrieve().bodyToMono(String.class).block();
            log.info("PPTX generation response: {}", resp);
            return resp;
        } catch (Exception e) {
            log.error("Error during PPTX generation call", e);
            return null;
        }
    }
}