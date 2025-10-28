package com.illunex.emsaasrestapi.chat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.illunex.emsaasrestapi.chat.dto.ResponseAIDTO;
import com.illunex.emsaasrestapi.chat.mapper.ChatFileMapper;
import com.illunex.emsaasrestapi.chat.mapper.ChatFileSlideMapper;
import com.illunex.emsaasrestapi.chat.mapper.ChatHistoryMapper;
import com.illunex.emsaasrestapi.chat.util.OpenAiSseParser;
import com.illunex.emsaasrestapi.chat.vo.ChatFileSlideVO;
import com.illunex.emsaasrestapi.chat.vo.ChatFileVO;
import com.illunex.emsaasrestapi.chat.vo.ChatHistoryVO;
import com.illunex.emsaasrestapi.common.CurrentMember;
import com.illunex.emsaasrestapi.common.aws.AwsS3Component;
import com.illunex.emsaasrestapi.common.aws.dto.AwsS3ResourceDTO;
import com.illunex.emsaasrestapi.common.code.EnumCode;
import com.illunex.emsaasrestapi.member.vo.MemberVO;
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
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AiProxyController {

    private final ChatFileSlideMapper chatFileSlideMapper;
    private final ChatHistoryMapper chatHistoryMapper;
    @Value("${ai.url}") String aiGptBase;

    private final UpstreamSseClient upstream;     // aiGptBase 로 SSE 프록시하는 클라이언트
    private final ChatService chatService;
    private final ToolResultService toolSvc;      // tool_result upsert & link(history_idx)
    private final ObjectMapper om;
    private final WebClient webClient;
    private final AwsS3Component awsS3Component;
    private final ChatFileMapper chatFileMapper;
    private final ConcurrentHashMap<String, ObjectNode> toolBuffer = new ConcurrentHashMap<>();

    @PostMapping(value = "ai/gpt/v2/api/convert-excel-graph")
    public ResponseEntity<?> convertExcelGraph(@CurrentMember MemberVO memberVO,
                                        @RequestParam("partnershipMemberIdx") Integer pmIdx,
                                        @RequestParam("chatHistoryIdx") Integer chatHistoryIdx) throws Exception {
        ChatHistoryVO history =  chatService.getChatHistory(memberVO, pmIdx, chatHistoryIdx);
        final String graphUrl = UriComponentsBuilder.fromHttpUrl(aiGptBase)
                .path("/v2/api/convert-excel-graph").toUriString();
        String graphResp = webClient.post().uri(graphUrl)
                .bodyValue(Map.of("md_text", history.getMessage()))
                .retrieve().bodyToMono(String.class).block();
        chatService.saveGraph(history, graphResp);
        return ResponseEntity.ok(om.readValue(chatService.normalizeGraphJson(graphResp, om), ResponseAIDTO.Graph.class));
    }

    @PostMapping(value = "ai/gpt/v2/api/generate-graph")
    public ResponseEntity<?> graphProxy(@CurrentMember MemberVO memberVO,
                                   @RequestParam("partnershipMemberIdx") Integer pmIdx,
                                   @RequestParam("chatHistoryIdx") Integer chatHistoryIdx) throws Exception {
        ChatHistoryVO history =  chatService.getChatHistory(memberVO, pmIdx, chatHistoryIdx);
        final String graphUrl = UriComponentsBuilder.fromHttpUrl(aiGptBase)
                .path("/v2/api/generate-graph").toUriString();
        String graphResp = webClient.post().uri(graphUrl)
                .bodyValue(Map.of("md_text", history.getMessage()))
                .retrieve().bodyToMono(String.class).block();
        chatService.saveGraph(history, graphResp);
        return ResponseEntity.ok(om.readValue(chatService.normalizeGraphJson(graphResp, om), ResponseAIDTO.Graph.class));
    }

    @PostMapping(value = "ai/gpt/v2/api/report-generate", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> proxy(@CurrentMember MemberVO memberVO,
                                            @RequestParam("partnershipMemberIdx") Integer pmIdx,
                                            @RequestParam(value = "title", required = false) String title,
                                            @RequestParam(value = "chatRoomIdx", required = false) Integer roomIdx,
                                            @RequestBody Map<String, String> body) {

        final String raw = String.valueOf(body.get("query"));
        final String query = raw == null ? "" : raw.trim();
        final var sseBuf = new AtomicReference<>(new StringBuilder(8192));
        final var proxyBuf = new StringBuilder(8192);
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
        final var lastPptxFlag  = new AtomicReference<>("no");  // 기본값 "no"
        final var lastDocxFlag  = new AtomicReference<>("no");  // 기본값 "no"

        // 툴 결과 즉시 upsert된 row들의 id 모음 → 완료 시 history_idx로 연결
        final List<Long> toolResultIds = new CopyOnWriteArrayList<>();
        final Set<ObjectNode> toolResults = new HashSet<>();
        final Set<String> mcpResultSet = new HashSet<>();

        // 업스트림 구독 (SSE 텍스트 조각을 그대로 흘려보내며, 동시에 파싱)
        Flux<String> stream = upstream.stream(aiGptBase, "/v2/api/report-generate", payload).share();

        // 4-1) 다운스트림 프록시: ★ 완결 JSON만 클라로 보냄
        Disposable dProxy = stream
                .publishOn(Schedulers.single()) // 파서 단일 스레드 보장
                .doOnNext(chunk -> { // tee는 원문 그대로 유지
                    try { tee.write(chunk.getBytes(StandardCharsets.UTF_8)); } catch (IOException ex) { throw new RuntimeException(ex); }
                })
                .map(chunk -> {
                    // ★ data: 라인만 뽑고 기존 drainSseDataJsons로 루트 JSON 경계 추출
                    String norm = normalizeSseDataLines(chunk);
                    return drainSseDataJsons(proxyBuf, norm); // 기존 함수 재사용 (buf 타입만 StringBuilder로 교체)
                })
                .flatMapIterable(list -> list)
                .subscribe(js -> {
                    try {
                        // ★ 프론트에는 항상 "완결 JSON 한 건"만 보냄
                        emitter.send(SseEmitter.event().name("message").data(js));
                    } catch (Exception ignore) {}
                }, e -> {
                    try { emitter.send(SseEmitter.event().name("error").data(e.getMessage() == null ? "" : e.getMessage())); } catch (Exception ignore) {}
                    emitter.complete();
                });

        // 4-2) 즉시 처리 파이프: “tool” 필드 포함된 이벤트 → tool_result upsert
        Disposable dTool = stream.subscribe(chunk -> {
            List<String> jsons = drainSseDataJsons(sseBuf.get(), chunk);
            if (jsons.isEmpty()) return;

            for (String js : jsons) {
                try {
                    JsonNode n = om.readTree(js);

                    // 보조 플래그/텍스트 추출(있으면 갱신)
                    String content = extractAssistantText(n);
                    if (content != null && !content.isBlank()) lastAssistant.set(content);
                    if (n.hasNonNull("category")) lastCategory.set(n.get("category").asText());
                    if (n.hasNonNull("pptx"))     lastPptxFlag.set(n.get("pptx").asText());
                    if (n.hasNonNull("docx"))     lastDocxFlag.set(n.get("docx").asText());

                    // 툴 이벤트 버퍼링 → 완료시 upsert
                    if (n.hasNonNull("tool")) {
                        String key = resolveToolCallId(n); // traceId/callId 최고, 없으면 fallback
                        toolBuffer.compute(key, (k, prev) -> {
                            ObjectNode acc = (prev != null) ? prev : om.createObjectNode();
                            ObjectNode cur = (n instanceof ObjectNode) ? (ObjectNode) n : (ObjectNode) om.valueToTree(n);
                            return mergeObjectNodes(om, acc, cur);
                        });

                        if (isTerminalForSearch(n)) { // ← 아래 함수
                            ObjectNode completed = toolBuffer.remove(key);
                            if (completed == null)
                                completed = (ObjectNode) (n instanceof ObjectNode ? n : om.valueToTree(n));

                            normalizeForPersist(completed, om); // 결과 슬림화/타임스탬프
//                            List<Long> ids = toolSvc.upsertToolPayload(completed.toString());

                            toolResults.clear();
                            toolResults.add(completed);
                        }
                    }
                    if (isMcpResult(n)) {
                        for (JsonNode item : n.get("mcp")) {
                            if (item == null || item.isNull()) continue;
                            mcpResultSet.add(item.asText());
                        }
                    }
                } catch (Exception ex) {
                    log.warn("tool/assistant parse fail: {}", ex.toString());
                }
            }
        }, e -> log.warn("tool/assistant parser error: {}", e.getMessage()));

        // 4-3) 완료 시점: 마지막 메시지 → chat_history 저장, 그 history_idx로 tool_result들 연결
        Disposable dDone = stream.ignoreElements().subscribe(null, null, () -> {
            final String all = tee.toString(StandardCharsets.UTF_8);

            var lastOkOpt = findLastOkEvent(all, om);

            if (lastOkOpt.isEmpty()) {
                // 최종 OK 이벤트가 없으면 안전 종료 (히스토리 저장/후처리 수행 안 함)
                try {
                    emitter.send(SseEmitter.event().name("final").data(
                            Map.of(
                                    "status", "no_ok_event",
                                    "reason", "최종 OK(status=200) 이벤트를 찾지 못했습니다."
                            )
                    ));
                    emitter.send(SseEmitter.event().name("done").data(Map.of("status", "ok")));
                } catch (Exception ignore) {}
                emitter.complete();
                return;
            }

            var lastOk = lastOkOpt.get();

            // 1) 메시지 결정: OK 이벤트의 message 우선 → 폴백 체인
            String okMessage = getTextOrEmpty(lastOk, "message");

            // 기존 폴백 계산
            String fixedText = extractLastAssistantFromStream(all, om);
            final String fallbackLast = OpenAiSseParser.extractLastMessageFromSequence(all);

            final String finalText = !isBlank(okMessage) ? okMessage
                    : (!isBlank(lastAssistant.get()) ? lastAssistant.get()
                    : (!isBlank(fixedText) ? fixedText
                    : (fallbackLast == null ? "" : fallbackLast)));

            // 2) 카테고리/코드
            String cate = getTextOrEmpty(lastOk, "category");
            if (isBlank(cate)) cate = lastCategory.get(); // 그래도 없으면 기존 값 폴백
            final String cateCode = safeCateCode(cate);
            String okPptx = ynOrNull(lastOk, "pptx");
            String okDocx = ynOrNull(lastOk, "docx");


            // 1) 최종 어시스턴트 메시지 저장 (flush 보장 권장)
            final int historyIdx = chatService.saveHistory(
                    chatRoomIdx,
                    EnumCode.ChatRoom.SenderType.ASSISTANT.getCode(),
                    cateCode,
                    finalText
            );

            // 2) tool_result ↔ history 링크
            if (!toolResults.isEmpty()) {
                try { chatService.insertChatToolByHistoryIdx(toolResults.stream().findFirst().get().toString(), historyIdx); }
                catch (Exception e) { log.error("linkResultsToHistory failed", e); }
            }

            if(!mcpResultSet.isEmpty()) {
                try { toolSvc.insertChatMcpArray(mcpResultSet, historyIdx); }
                catch (Exception e) { log.error("linkResultsToHistory failed", e); }
            }

            // 스트림 전체에서 마지막 플래그 재보정
            Map<String, String> lastFlags = extractLastFlagsFromStream(all, om);
            if (okPptx == null && lastFlags.containsKey("pptx")) okPptx = lastFlags.get("pptx");
            if (okDocx == null && lastFlags.containsKey("docx")) okDocx = lastFlags.get("docx");

            if (okPptx != null) lastPptxFlag.set(okPptx);
            if (okDocx != null) lastDocxFlag.set(okDocx);

            boolean needPpt  = "yes".equalsIgnoreCase(lastPptxFlag.get());
            boolean needDocx = "yes".equalsIgnoreCase(lastDocxFlag.get());
            boolean hasPost  = (needPpt || needDocx);

            String finalStatus = hasPost ? "processing" : "ok";

            // 3) 프론트에 "final" 이벤트 먼저 발사 (최종 메시지 고정)
            var finalPayload = Map.of(
                    "status", finalStatus,
                    "history", Map.of(
                            "idx", historyIdx,
                            "chatRoomIdx", chatRoomIdx,
                            "senderType", EnumCode.ChatRoom.SenderType.ASSISTANT.getCode(),
                            "categoryType", cateCode,
                            "message", finalText
                    ),
                    "flags", Map.of(
                            "pptx", lastPptxFlag.get(),
                            "docx", lastDocxFlag.get()
                    )
            );
            try { emitter.send(SseEmitter.event().name("final").data(finalPayload)); }
            catch (Exception ignore) {}


            if (!hasPost) {
                // 4-A) 후처리 없으면 바로 done
                try { emitter.send(SseEmitter.event().name("done").data(Map.of("status", "ok"))); } catch (Exception ignore) {}
                emitter.complete();
                return;
            }

            // 4-B) 후처리 있으면 "파일 생성중..." 상태 먼저 보내고 실제 작업은 백그라운드 스레드에서
            try {
                emitter.send(SseEmitter.event().name("status").data(
                        Map.of("stage", "postprocess", "message", "파일 생성중...")
                ));
            } catch (Exception ignore) {}
            ChatHistoryVO professionalMsg = chatHistoryMapper.selectByChatRoomIdxAndCategoryTypeOrderByCreateDateDesc(chatRoomIdx, EnumCode.ChatHistory.CategoryType.PROFESSIONAL.getCode()).get(0);

            // 5) 외부 API 호출 + S3 업로드 + DB 인서트 (블로킹 작업 → boundedElastic)
            reactor.core.publisher.Mono.fromCallable(() -> {
                Map<String, Object> result = new java.util.HashMap<>();

                if (needPpt) {
                    JsonNode pptRes = callPptxGenerate(professionalMsg.getMessage(), pmIdx, historyIdx);

                    if (pptRes != null && pptRes.path("error").isMissingNode()) {
                        Map<String, Object> pptMap = new LinkedHashMap<>();
                        pptMap.put("attachmentIdx", pptRes.path("chat_file_idx").asInt(-1));
                        pptMap.put("filename",      pptRes.path("filename").asText(""));
                        pptMap.put("filesize",      pptRes.path("filesize").asLong(0L));
                        pptMap.put("s3_url",        pptRes.path("s3_url").asText(""));

                        // slides 배열 → List<String>
                        List<String> slides = toStringList(pptRes.get("slides"));
                        if (!slides.isEmpty()) pptMap.put("slides", slides);

                        result.put("pptx", pptMap);
                    } else {
                        result.put("pptx", Map.of("error", pptRes == null
                                ? "pptx generation failed"
                                : pptRes.path("message").asText("pptx generation failed")));
                    }
                }

                if (needDocx) {
                    JsonNode docx = callDocxGenerate(professionalMsg.getMessage(), pmIdx, historyIdx);

                    if (docx != null && docx.path("error").isMissingNode()) {
                        Map<String, Object> docxMap = new LinkedHashMap<>();
                        docxMap.put("attachmentIdx", docx.path("chat_file_idx").asInt(-1));
                        docxMap.put("filename",      docx.path("filename").asText(""));
                        docxMap.put("filesize",      docx.path("filesize").asLong(0L));
                        docxMap.put("s3_url",        docx.path("s3_url").asText(""));

                        result.put("docx", docxMap);
                    } else {
                        result.put("docx", Map.of("error", docx == null
                                ? "docx generation failed"
                                : docx.path("message").asText("docx generation failed")));
                    }
                }

                return result;
            })
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe(post -> {
                // 6) 결과 전달
                try { emitter.send(SseEmitter.event().name("assets").data(post)); } catch (Exception ignore) {}
                try { emitter.send(SseEmitter.event().name("done").data(Map.of("status", "ok"))); } catch (Exception ignore) {}
                emitter.complete();
            }, err -> {
                log.error("postprocess failed", err);
                try {
                    emitter.send(SseEmitter.event().name("assets").data(
                            Map.of("error", "파일 생성 중 오류: " + err.getMessage())
                    ));
                    emitter.send(SseEmitter.event().name("done").data(Map.of("status", "ok")));
                } catch (Exception ignore) {}
                emitter.complete();
            });
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
        headers.set("Content-Type", "text/event-stream; charset=utf-8");
        headers.setCacheControl("no-cache");
        headers.setConnection("keep-alive");
        headers.add("X-Accel-Buffering", "no"); // nginx 쓰면 버퍼링 차단
        headers.add("X-Chat-Room-Idx", String.valueOf(chatRoomIdx));
        headers.add("Access-Control-Expose-Headers", "X-Chat-Room-Idx");
        return new ResponseEntity<>(emitter, headers, HttpStatus.OK);
    }

    private boolean isMcpResult(JsonNode n) {
        return n.hasNonNull("mcp") && n.get("mcp").isArray();
    }

    private JsonNode callDocxGenerate(String mdText, Integer pmIdx, Integer historyIdx) {
        try {
            String genUrl = UriComponentsBuilder.fromHttpUrl(aiGptBase)
                    .path("/v2/api/generate-docx").toUriString();

            byte[] bytes = null;
            String ctype = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            String fname = "document.docx";
            long size = 0L;

            var fileEntity = webClient.post().uri(genUrl)
                    .bodyValue(Map.of("md_text", mdText))
                    .retrieve()
                    .toEntity(byte[].class)
                    .block();
            bytes = fileEntity.getBody();
            HttpHeaders headers = fileEntity.getHeaders();
            ctype  = headers.getContentType() != null ? headers.getContentType().toString() : ctype;
            size   = headers.getContentLength() > 0 ? headers.getContentLength() : bytes.length;
            fname  = resolveFileName(headers, fname);

            // S3 업로드는 다운로드가 있을 때만
            AwsS3ResourceDTO s3 = null;
            if (bytes != null) {
                try (var is = new java.io.ByteArrayInputStream(bytes)) {
                    s3 = AwsS3ResourceDTO.builder()
                            .fileName(fname)
                            .s3Resource(awsS3Component.upload(
                                    is, AwsS3Component.FolderType.LLMGeneratedPPTX,
                                    pmIdx.toString(), ctype, fname))
                            .build();
                }
            }

            // DB 인서트 (DOCX 실제로 생겼을 때만)
            Long chatFileIdx = null;
            if (s3 != null) {
                ChatFileVO chatFileVO = new ChatFileVO();
                chatFileVO.setChatHistoryIdx(historyIdx);
                chatFileVO.setFileName(firstNonBlank(s3.getOrgFileName(), fname));
                chatFileVO.setFileUrl(s3.getUrl());
                chatFileVO.setFilePath(s3.getPath());
                chatFileVO.setFileSize(s3.getSize() != null ? s3.getSize() : size);
                chatFileVO.setFileCd(EnumCode.ChatFile.FileCd.DOCS.getCode());
                chatFileMapper.insertByChatFileVO(chatFileVO);
                chatFileIdx = chatFileVO.getIdx();
            }
            var obj = om.createObjectNode();

            if (s3 != null) {
                obj.put("s3_url", s3.getUrl() == null ? "" : s3.getUrl());
            }
            obj.put("filename", chatFileIdx == null ? fname : firstNonBlank(s3.getOrgFileName(), fname));
            obj.put("filesize", chatFileIdx == null ? size : (s3.getSize() == null ? 0L : s3.getSize()));
            if (chatFileIdx != null) obj.put("chat_file_idx", chatFileIdx);

            return obj;

        } catch (Exception e) {
            log.error("Error during DOCX generation/download/upload", e);
            return om.createObjectNode().put("error", "docx_generation_failed")
                    .put("message", e.getMessage() == null ? "" : e.getMessage());
        }
    }

    // 최근 히스토리를 프롬프트 압축 포맷으로 변환(예: [{"u":"..."},{"a":"..."}])
    private String toHistoryJsonString(List<ChatHistoryVO> histories) {
        if (histories == null || histories.isEmpty()) return "[]";
        try {
            var list = histories.stream()
                    .map(h -> {
                        String key = h.getSenderType().equals(EnumCode.ChatRoom.SenderType.USER.getCode()) ? "u" : "a";
                        String msg = h.getMessage() == null ? "" : h.getMessage();
                        String category = EnumCode.getCodeDesc(h.getCategoryType()) == null ? "" : EnumCode.getCodeDesc(h.getCategoryType());

                        return Map.of(
                                key, msg,
                                "category", category
                        );
                    }).toList();
            return om.writeValueAsString(list);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 다양한 공급자 포맷에서 어시스턴트 텍스트 후보를 뽑아냄
    private static String extractAssistantText(JsonNode n) {
        if (n.hasNonNull("content")) return n.get("content").asText();
        if (n.has("delta") && n.get("delta").hasNonNull("content")) return n.get("delta").get("content").asText();
        if (n.has("message") && n.get("message").hasNonNull("content")) return n.get("message").get("content").asText();
        if (n.has("message") && n.get("message").isTextual()) return n.get("message").asText(); // ★ 추가
        if (n.hasNonNull("text")) return n.get("text").asText();
        if (n.has("message") && n.get("message").isTextual()) return n.get("message").asText();
        return null;
    }


    // 필요 시 PPTX 생성 콜(엔드포인트 통일)
    private JsonNode callPptxGenerate(String mdText, Integer pmIdx, Integer historyIdx) {
        try {
            String genUrl = UriComponentsBuilder.fromHttpUrl(aiGptBase)
                    .path("/v2/api/generate-pptx").toUriString();

            String genResp = webClient.post().uri(genUrl)
                    .bodyValue(Map.of("md_text", mdText))
                    .retrieve().bodyToMono(String.class).block();
            JsonNode n = om.readTree(genResp);

            boolean hasSlides = n.has("slides") && n.get("slides").isArray();
            boolean hasDl = n.hasNonNull("pptx_download_url") && !n.get("pptx_download_url").asText().isBlank();
            boolean hasTitle = n.hasNonNull("title") && !n.get("title").asText().isBlank();

            byte[] bytes = null;
            String ctype = "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            String fname = "slides.pptx";
            long size = 0L;

            if (hasDl) {
                String dlUrl = UriComponentsBuilder.fromHttpUrl(aiGptBase)
                        .path(n.get("pptx_download_url").asText()).toUriString();

                var fileEntity = webClient.get().uri(dlUrl)
                        .accept(MediaType.APPLICATION_OCTET_STREAM)
                        .retrieve()
                        .toEntity(byte[].class)
                        .block();

                if (fileEntity == null || fileEntity.getBody() == null) {
                    throw new IllegalStateException("Empty PPTX download response");
                }
                bytes = fileEntity.getBody();
                HttpHeaders headers = fileEntity.getHeaders();
                ctype  = headers.getContentType() != null ? headers.getContentType().toString() : ctype;
                size   = headers.getContentLength() > 0 ? headers.getContentLength() : bytes.length;
                fname  = resolveFileName(headers, fname);
            }

            // S3 업로드는 다운로드가 있을 때만
            AwsS3ResourceDTO s3 = null;
            if (bytes != null) {
                try (var is = new java.io.ByteArrayInputStream(bytes)) {
                    s3 = AwsS3ResourceDTO.builder()
                            .fileName(fname)
                            .s3Resource(awsS3Component.upload(
                                    is, AwsS3Component.FolderType.LLMGeneratedPPTX,
                                    pmIdx.toString(), ctype, fname))
                            .build();
                }
            }

            // DB 인서트 (PPTX가 실제로 생겼을 때만)
            Long chatFileIdx = null;
            if (s3 != null) {
                ChatFileVO chatFileVO = new ChatFileVO();
                chatFileVO.setChatHistoryIdx(historyIdx);
                chatFileVO.setFileName(n.get("title").asText().isBlank() ? fname : n.get("title").asText());
                chatFileVO.setFileUrl(s3.getUrl());
                chatFileVO.setFilePath(s3.getPath());
                chatFileVO.setFileSize(s3.getSize() != null ? s3.getSize() : size);
                chatFileVO.setFileCd(EnumCode.ChatFile.FileCd.PPTX.getCode());
                chatFileMapper.insertByChatFileVO(chatFileVO);
                chatFileIdx = chatFileVO.getIdx();
            }

            // 응답 구성: slides 포함(배열 그대로), 파일 메타 포함
            var obj = n.isObject() ? (com.fasterxml.jackson.databind.node.ObjectNode) n : om.createObjectNode();
            if (hasSlides) {
                obj.set("slides", n.get("slides")); // ★ 여기 핵심: asText() 말고 set()
                // DB 저장은 chatFileIdx가 있을 때만 (PPTX 업로드 성공 케이스)
                if (chatFileIdx != null) {
                    JsonNode slidesNode = n.get("slides");
                    if (slidesNode.isArray()) {
                        int page = 1; // 1부터 시작이 일반적
                        for (JsonNode slideNode : slidesNode) {
                            ChatFileSlideVO slideVO = new ChatFileSlideVO();
                            slideVO.setChatFileIdx(chatFileIdx);
                            slideVO.setPage(page++);
                            // asText()로 HTML 원문을 저장 (이스케이프 제거, 프론트에서 바로 렌더 용이)
                            slideVO.setContent(slideNode.asText(""));
                            chatFileSlideMapper.insertByChatFileSlideVO(slideVO);
                        }
                        // 필요하면 개수도 붙여주자
                        obj.put("slide_count", slidesNode.size());
                    }
                } else {
                    // PPTX 파일 없이 슬라이드만 있는 경우: DB 저장 스킵(혹은 placeholder ChatFile 생성 전략 선택)
                    log.info("slides present but chatFileIdx is null; skip persisting slides.");
                }
            }
            if (s3 != null) {
                obj.put("s3_url", s3.getUrl() == null ? "" : s3.getUrl());
            }
            obj.put("filename", chatFileIdx == null ? fname : firstNonBlank(s3.getOrgFileName(), fname));
            obj.put("filesize", chatFileIdx == null ? size : (s3.getSize() == null ? 0L : s3.getSize()));
            if (chatFileIdx != null) obj.put("chat_file_idx", chatFileIdx);

            return obj;

        } catch (Exception e) {
            log.error("Error during PPTX generation/download/upload", e);
            return om.createObjectNode().put("error", "pptx_generation_failed")
                    .put("message", e.getMessage() == null ? "" : e.getMessage());
        }
    }

    private String firstNonBlank(String a, String b) {
        return (a != null && !a.isBlank()) ? a : b;
    }

    private String resolveFileName(HttpHeaders headers, String fallback) {
        String cd = headers.getFirst(HttpHeaders.CONTENT_DISPOSITION);
        if (cd != null) {
            try {
                ContentDisposition d = ContentDisposition.parse(cd);
                if (d.getFilename() != null && !d.getFilename().isBlank()) {
                    return d.getFilename();
                }
            } catch (Exception ignore) {}
        }
        return fallback;
    }
    private List<String> toStringList(JsonNode node) {
        if (node == null || !node.isArray()) return Collections.emptyList();
        List<String> out = new ArrayList<>(node.size());
        node.forEach(e -> out.add(e.asText(""))); // null 방지
        return out;
    }

    private String safeCateCode(String cate) {
        if (cate == null || cate.isBlank() || "null".equalsIgnoreCase(cate)) {
            return EnumCode.ChatHistory.CategoryType.GENERAL.getCode();
        }
        try { return EnumCode.ChatHistory.CategoryType.getCodeByValue(cate); }
        catch (IllegalArgumentException ex) { return EnumCode.ChatHistory.CategoryType.GENERAL.getCode(); }
    }

    // tee(전체 SSE 텍스트)를 \n\n 프레임 단위로 쪼개서,
// 각 프레임의 data: 라인을 합쳐 JSON을 만들고,
// pptx/docs의 "마지막으로 관측된 값"을 반환한다.
    private static Map<String, String> extractLastFlagsFromStream(String all, ObjectMapper om) {
        Map<String, String> out = new HashMap<>();
        JsonNode last = extractLastDataJson(all, om);
        if (last == null) return out;
        if (last.hasNonNull("pptx")) out.put("pptx", last.get("pptx").asText());
        if (last.hasNonNull("docx")) out.put("docx", last.get("docx").asText());
        return out;
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    // tee 전체에서 \n\n 프레임 단위로 data: 라인을 합쳐 JSON을 만들고,
    // content/delta.content/message.content/message(문자열)/text 순으로
    // "마지막 유효 텍스트"를 찾아 반환.
    private static String extractLastAssistantFromStream(String all, ObjectMapper om) {
        if (isBlank(all)) return "";
        final String SEP = "\n\n";
        int from = 0, cut;
        String last = "";
        while ((cut = all.indexOf(SEP, from)) >= 0) {
            String frame = all.substring(from, cut);
            from = cut + SEP.length();

            StringBuilder data = new StringBuilder();
            for (String line : frame.split("\n")) {
                if (line.startsWith("data:")) {
                    if (data.length() > 0) data.append('\n');
                    data.append(line.substring(5).trim());
                }
            }
            String s = data.toString().trim();
            if (s.isEmpty() || s.charAt(0) != '{' || s.charAt(s.length()-1) != '}') continue;

            try {
                JsonNode n = om.readTree(s);
                // 1) content
                if (n.hasNonNull("content")) last = n.get("content").asText();
                // 2) delta.content
                if (n.has("delta") && n.get("delta").hasNonNull("content")) last = n.get("delta").get("content").asText();
                // 3) message.content (객체형)
                if (n.has("message") && n.get("message").hasNonNull("content")) last = n.get("message").get("content").asText();
                // 4) message (문자열형)
                if (n.has("message") && n.get("message").isTextual()) last = n.get("message").asText();
                // 5) text
                if (n.hasNonNull("text")) last = n.get("text").asText();
            } catch (Exception ignore) {}
        }
        return last == null ? "" : last;
    }

    // 교체: data: 유무 모두 처리 (없으면 텍스트에서 마지막 JSON 객체를 직접 찾음)
    private static JsonNode extractLastDataJson(String all, ObjectMapper om) {
        if (all == null || all.isEmpty()) return null;

        // 1) data: 라인이 있는 SSE 포맷 처리
        if (all.contains("data:")) {
            final String SEP = "\n\n";
            int from = 0, cut;
            JsonNode last = null;
            while ((cut = all.indexOf(SEP, from)) >= 0) {
                String frame = all.substring(from, cut);
                from = cut + SEP.length();

                StringBuilder data = new StringBuilder();
                for (String line : frame.split("\n")) {
                    if (line.startsWith("data:")) {
                        if (data.length() > 0) data.append('\n');
                        data.append(line.substring(5).trim());
                    }
                }
                String s = data.toString().trim();
                if (s.startsWith("{") && s.endsWith("}")) {
                    try { last = om.readTree(s); } catch (Exception ignore) {}
                }
            }
            if (last != null) return last; // 찾았으면 종료
            // 못 찾았으면 아래 루즈 파서로 폴백
        }

        // 2) data: 가 없는 “그냥 JSON 텍스트” 처리 — 마지막 완전한 JSON 객체를 찾아 파싱
        int start = -1, depth = 0;
        boolean inStr = false, esc = false;
        for (int i = 0; i < all.length(); i++) {
            char c = all.charAt(i);
            if (inStr) {
                if (esc) { esc = false; continue; }
                if (c == '\\') { esc = true; continue; }
                if (c == '"') { inStr = false; }
                continue;
            }
            if (c == '"') { inStr = true; continue; }
            if (c == '{') {
                if (depth == 0) start = i;
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0 && start >= 0) {
                    String obj = all.substring(start, i + 1);
                    try { return om.readTree(obj); } catch (Exception ignore) {}
                    start = -1; // 다음 후보로
                }
            }
        }
        return null;
    }

    private Optional<com.fasterxml.jackson.databind.node.ObjectNode> findLastOkEvent(String all, ObjectMapper om) {
        if (all == null || all.isBlank()) return Optional.empty();
        try (var p = om.getFactory().createParser(all)) {
            com.fasterxml.jackson.databind.node.ObjectNode lastOk = null;
            while (p.nextToken() != null) {
                // 이벤트가 연속된 JSON object라고 가정
                var node = om.readTree(p);
                if (node != null && node.isObject()) {
                    var obj = (com.fasterxml.jackson.databind.node.ObjectNode) node;
                    var st = obj.get("status");
                    boolean isOk = false;
                    if (st != null) {
                        if (st.isInt()) isOk = (st.intValue() == 200);
                        else if (st.isTextual()) isOk = "200".equals(st.asText());
                    }
                    if (isOk) lastOk = obj;
                }
            }
            return Optional.ofNullable(lastOk);
        } catch (Exception e) {
            log.warn("findLastOkEvent parse failed", e);
            return Optional.empty();
        }
    }

    private static String getTextOrEmpty(JsonNode n, String field) {
        var v = n == null ? null : n.get(field);
        return (v == null || v.isNull()) ? "" : v.asText("");
    }

    private static String ynOrNull(JsonNode n, String field) {
        var v = n == null ? null : n.get(field);
        if (v == null || v.isNull()) return null;
        var t = v.asText("");
        if (t.isBlank()) return null;
        return t; // "yes"/"no" 그대로
    }

    private boolean isTerminalForSearch(JsonNode n) {
        if (!"get_search_result_by_query_tool".equals(n.path("tool").asText())) return false;
        JsonNode results = n.get("results");
        return results != null && results.isArray() && results.size() > 0;
    }

    private void normalizeForPersist(ObjectNode obj, ObjectMapper om) {
        if ("get_search_result_by_query_tool".equals(obj.path("tool").asText())) {
            ArrayNode src = (ArrayNode) obj.path("results");
            ArrayNode arr = om.createArrayNode();
            int limit = Math.min(src.size(), 10);
            for (int i = 0; i < limit; i++) {
                JsonNode it = src.get(i);
                ObjectNode slim = om.createObjectNode();
                slim.put("title", it.path("title").asText(null));
                slim.put("url",   it.path("url").asText(null));
                arr.add(slim);
            }
            obj.set("results", arr);
        }
        obj.put("@upsert_at", java.time.OffsetDateTime.now().toString());
    }

    private String resolveToolCallId(JsonNode n) {
        // 1) 표준 필드 우선
        if (n.hasNonNull("tool_call_id")) return n.get("tool_call_id").asText();
        if (n.hasNonNull("callId")) return n.get("callId").asText();
        if (n.hasNonNull("id")) return n.get("id").asText();
        // 2) 최후의 수단: tool 이름+args 해시(가능하면 쓰지 말자)
        String seed = (n.hasNonNull("tool") ? n.get("tool").toString() : "") +
                (n.hasNonNull("arguments") ? n.get("arguments").toString() : "");
        return Integer.toHexString(seed.hashCode());
    }

    private ObjectNode mergeObjectNodes(ObjectMapper om, ObjectNode base, JsonNode add) {
        // 얕은 병합: 같은 키는 add가 덮어씀. 필요시 깊은 병합 구현
        add.fields().forEachRemaining(e -> base.set(e.getKey(), e.getValue()));
        return base;
    }

    public static List<String> drainSseDataJsons(StringBuilder buf, String chunk) {
        if (chunk == null || chunk.isEmpty()) return java.util.Collections.emptyList();
        // SSE "data:" 라인 형태로 올 수 있으면 여기서 전처리(선택)
        // chunk = normalizeSseDataLines(chunk);

        buf.append(chunk);

        List<String> out = new java.util.ArrayList<>();
        int n = buf.length();
        int depth = 0;
        boolean inString = false;
        boolean escape = false;
        int start = -1; // 루트 JSON 시작 인덱스

        for (int i = 0; i < n; i++) {
            char c = buf.charAt(i);

            // 문자열 상태 처리
            if (inString) {
                if (escape) { escape = false; continue; }
                if (c == '\\') { escape = true; continue; }
                if (c == '"')  { inString = false; }
                continue; // 문자열 안에서는 중첩 카운팅 안 함
            } else {
                // 문자열 시작
                if (c == '"') { inString = true; continue; }
                // 루트 시작을 처음 만나는 '{' or '['에서 기록
                if (c == '{' || c == '[') {
                    if (depth == 0) start = i;
                    depth++;
                } else if (c == '}' || c == ']') {
                    depth--;
                    if (depth == 0 && start >= 0) {
                        // 완전한 하나의 루트 JSON 확보
                        String json = buf.substring(start, i + 1);
                        out.add(json);
                        start = -1; // 다음 루트를 위해 리셋
                    }
                } else {
                    // 공백/개행/쉼표 등은 무시; 루트값 사이에 있을 수 있음
                }
            }
        }

        // 처리된 부분 잘라내고, 미완성 꼬리만 남김
        if (!out.isEmpty()) {
            int lastEnd = buf.lastIndexOf(out.get(out.size() - 1)) + out.get(out.size() - 1).length();
            buf.delete(0, lastEnd);
        } else {
            // 아무것도 못 뽑았는데 버퍼가 너무 크면(예: 쓰레기/로그) 컷
            final int MAX_BUF = 1 << 20; // 1MB
            if (buf.length() > MAX_BUF) buf.setLength(0);
        }

        return out;
    }

    private static String normalizeSseDataLines(String chunk) {
        if (chunk == null || chunk.isEmpty()) return "";
        if (chunk.indexOf("data:") < 0 && chunk.indexOf("event:") < 0) return chunk; // 그냥 원문 텍스트
        StringBuilder sb = new StringBuilder(chunk.length());
        for (String line : chunk.split("\n")) {
            if (line.startsWith("data:")) {
                sb.append(line.substring(5).trim()).append('\n'); // NDJSON/루트 JSON 모두 지원
            }
        }
        return sb.toString();
    }
}