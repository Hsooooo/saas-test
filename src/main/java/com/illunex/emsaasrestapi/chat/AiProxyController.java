package com.illunex.emsaasrestapi.chat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.illunex.emsaasrestapi.chat.dto.ResponseAIDTO;
import com.illunex.emsaasrestapi.chat.mapper.ChatFileMapper;
import com.illunex.emsaasrestapi.chat.mapper.ChatFileSlideMapper;
import com.illunex.emsaasrestapi.chat.util.OpenAiSseParser;
import com.illunex.emsaasrestapi.chat.vo.ChatFileSlideVO;
import com.illunex.emsaasrestapi.chat.vo.ChatFileVO;
import com.illunex.emsaasrestapi.chat.vo.ChatHistoryVO;
import com.illunex.emsaasrestapi.common.CurrentMember;
import com.illunex.emsaasrestapi.common.CustomException;
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
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AiProxyController {

    private final ChatFileSlideMapper chatFileSlideMapper;
    @Value("${ai.url}") String aiGptBase;

    private final UpstreamSseClient upstream;     // aiGptBase 로 SSE 프록시하는 클라이언트 (Flux<String> data 청크)
    private final ChatService chatService;        // 채팅방/히스토리 저장
    private final ToolResultService toolSvc;      // tool_result upsert & link(history_idx)
    private final ObjectMapper om;
    private final WebClient webClient;
    private final AwsS3Component awsS3Component;
    private final ChatFileMapper chatFileMapper;

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
        final var lastDocsFlag  = new AtomicReference<>("no");  // 기본값 "no"

        // 툴 결과 즉시 upsert된 row들의 id 모음 → 완료 시 history_idx로 연결
        final List<Long> toolResultIds = new CopyOnWriteArrayList<>();

        // 업스트림 구독 (SSE 텍스트 조각을 그대로 흘려보내며, 동시에 파싱)
        Flux<String> streamRaw = upstream.stream(aiGptBase, "/v2/api/report-generate", payload).share();
        Flux<SseFrame> frames = frameSse(streamRaw).share();

        // 4-1) 다운스트림으로 프록시 + tee 에 기록
        Disposable dProxy = frames.subscribe(f -> {
            try { tee.write((f.data() + "\n").getBytes(StandardCharsets.UTF_8)); } catch (IOException ex) { /* noop */ }
            try {
                var ev = SseEmitter.event();
                if (f.event() != null && !f.event().isBlank()) ev.name(f.event());
                if (f.id() != null && !f.id().isBlank())       ev.id(f.id());
                // data는 완전한 JSON 문자열
                emitter.send(ev.data(f.data(), MediaType.APPLICATION_JSON));
            } catch (Exception ignore) {}
        }, e -> {
            try { emitter.send("event: error\ndata: " + (e.getMessage() == null ? "" : e.getMessage()) + "\n\n"); } catch (Exception ignore) {}
            emitter.complete();
        });

        // 4-2) 즉시 처리 파이프: “tool” 필드 포함된 이벤트 → tool_result upsert
        Disposable dTool = frames.subscribe(f -> {
            try {
                JsonNode n = om.readTree(f.data()); // 항상 완전한 JSON
                if (n.hasNonNull("tool")) {
                    List<Long> ids = toolSvc.upsertToolPayload(n.toString());
                    if (ids != null && !ids.isEmpty()) toolResultIds.addAll(ids);
                }
                String content = extractAssistantText(n);
                if (content != null && !content.isBlank()) lastAssistant.set(content);
                if (n.has("category") && !n.get("category").isNull()) lastCategory.set(n.get("category").asText());
                if (n.hasNonNull("pptx")) lastPptxFlag.set(n.get("pptx").asText());
                if (n.hasNonNull("docs")) lastDocsFlag.set(n.get("docs").asText());
            } catch (Exception ignore) {}
        }, e -> log.warn("tool/assistant parser error: {}", e.getMessage()));

        // 4-3) 완료 시점: 마지막 메시지 → chat_history 저장, 그 history_idx로 tool_result들 연결
        Disposable dDone = frames.ignoreElements().subscribe(null, null, () -> {
            final String all = tee.toString(StandardCharsets.UTF_8);
            final String fallbackLast = OpenAiSseParser.extractLastMessageFromSequence(all);
            final String finalText = (lastAssistant.get() == null || lastAssistant.get().isBlank())
                    ? (fallbackLast == null ? "" : fallbackLast)
                    : lastAssistant.get();

            final String cate = lastCategory.get();
            final String cateCode = safeCateCode(cate);

            // 1) 최종 어시스턴트 메시지 저장 (flush 보장 권장)
            final int historyIdx = chatService.saveHistory(
                    chatRoomIdx,
                    EnumCode.ChatRoom.SenderType.ASSISTANT.getCode(),
                    cateCode,
                    finalText
            );

            // 2) tool_result ↔ history 링크
            if (!toolResultIds.isEmpty()) {
                try { toolSvc.linkResultsToHistory(toolResultIds, historyIdx); }
                catch (Exception e) { log.error("linkResultsToHistory failed", e); }
            }

            // 3) 프론트에 "final" 이벤트 먼저 발사 (최종 메시지 고정)
            var finalPayload = Map.of(
                    "status", "ok",
                    "history", Map.of(
                            "idx", historyIdx,
                            "chatRoomIdx", chatRoomIdx,
                            "senderType", EnumCode.ChatRoom.SenderType.ASSISTANT.getCode(),
                            "categoryType", cateCode,
                            "message", finalText
                    ),
                    "flags", Map.of(
                            "pptx", lastPptxFlag.get(),
                            "docs", lastDocsFlag.get()
                    )
            );
            try { emitter.send(SseEmitter.event().name("final").data(finalPayload)); }
            catch (Exception ignore) {}

            boolean needPpt = "yes".equalsIgnoreCase(lastPptxFlag.get());
            boolean needDocs = "yes".equalsIgnoreCase(lastDocsFlag.get());

            if (!(needPpt || needDocs)) {
                // 4-A) 후처리 없으면 바로 done
                try { emitter.send(SseEmitter.event().name("done").data("ok")); } catch (Exception ignore) {}
                emitter.complete();
                return;
            }

            // 4-B) 후처리 있으면 "파일 생성중..." 상태 먼저 보내고 실제 작업은 백그라운드 스레드에서
            try {
                emitter.send(SseEmitter.event().name("status").data(
                        Map.of("stage", "postprocess", "message", "파일 생성중...")
                ));
            } catch (Exception ignore) {}

            // 5) 외부 API 호출 + S3 업로드 + DB 인서트 (블로킹 작업 → boundedElastic)
            reactor.core.publisher.Mono.fromCallable(() -> {
                Map<String, Object> result = new java.util.HashMap<>();

                if (needPpt) {
                    JsonNode pptRes = callPptxGenerate(finalText, pmIdx, historyIdx);

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

                if (needDocs) {
//                    JsonNode docRes = callDocsGenerate(finalText); // 너 쪽 docs 생성/업로드 메서드
//                    Integer attachIdx = saveAttachmentMeta(historyIdx, docRes, "DOCS");
//                    result.put("docs", Map.of(
//                            "attachmentIdx", attachIdx,
//                            "s3_bucket", docRes.path("s3_bucket").asText(null),
//                            "s3_key", docRes.path("s3_key").asText(null),
//                            "presigned_url", docRes.path("s3_presigned_url").asText(null)
//                    ));
                }

                return result;
            })
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe(post -> {
                // 6) 결과 전달
                try { emitter.send(SseEmitter.event().name("assets").data(post)); } catch (Exception ignore) {}
                try { emitter.send(SseEmitter.event().name("done").data("ok")); } catch (Exception ignore) {}
                emitter.complete();
            }, err -> {
                log.error("postprocess failed", err);
                try {
                    emitter.send(SseEmitter.event().name("assets").data(
                            Map.of("error", "파일 생성 중 오류: " + err.getMessage())
                    ));
                    emitter.send(SseEmitter.event().name("done").data("ok"));
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
        headers.add("X-Accel-Buffering", "no"); // nginx 사용 시
        headers.add("X-Chat-Room-Idx", String.valueOf(chatRoomIdx));
        headers.add("Access-Control-Expose-Headers", "X-Chat-Room-Idx");
        return new ResponseEntity<>(emitter, headers, HttpStatus.OK);
    }

    private JsonNode callDocsGenerate(String mdText, Integer pmIdx, Integer historyIdx) {
        try {
            String genUrl = UriComponentsBuilder.fromHttpUrl(aiGptBase)
                    .path("/v2/api/generate-docs").toUriString();

            String genResp = webClient.post().uri(genUrl)
                    .bodyValue(Map.of("md_text", mdText))
                    .retrieve().bodyToMono(String.class).block();
            JsonNode n = om.readTree(genResp);

            boolean hasSlides = n.has("slides") && n.get("slides").isArray();
            boolean hasDl = n.hasNonNull("pptx_download_url") && !n.get("pptx_download_url").asText().isBlank();

            byte[] bytes = null;
            String ctype = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
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
                chatFileVO.setFileName(firstNonBlank(s3.getOrgFileName(), fname));
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
                chatFileVO.setFileName(firstNonBlank(s3.getOrgFileName(), fname));
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

    record SseFrame(String event, String id, String data) {}

    // Flux<String> 청크 → SSE 프레임 단위로 분리
    static Flux<SseFrame> frameSse(Flux<String> chunks) {
        final String SEP = "\n\n";
        return chunks
                .scan(new StringBuilder(), (buf, s) -> { buf.append(s); return buf; })
                .flatMapIterable(buf -> {
                    String all = buf.toString();
                    int cut, from = 0;
                    List<SseFrame> out = new ArrayList<>();
                    while ((cut = all.indexOf(SEP, from)) >= 0) {
                        String frame = all.substring(from, cut);
                        from = cut + SEP.length();

                        String event = null, id = null;
                        StringBuilder data = new StringBuilder();
                        for (String line : frame.split("\n")) {
                            if (line.startsWith(":")) continue;
                            if (line.startsWith("event:")) { event = line.substring(6).trim(); continue; }
                            if (line.startsWith("id:"))    { id    = line.substring(3).trim(); continue; }
                            if (line.startsWith("data:"))  {
                                if (data.length() > 0) data.append('\n');
                                data.append(line.substring(5).trim());
                            }
                        }
                        if (data.length() > 0) out.add(new SseFrame(event, id, data.toString()));
                    }
                    buf.setLength(0);
                    buf.append(all.substring(from));
                    return out;
                });
    }
    private String safeCateCode(String cate) {
        // null, "", 미등록 value → GENERAL 로 폴백(원하면 SIMPLE로 바꿔도 됨)
        if (cate == null || cate.isBlank()) {
            return EnumCode.ChatHistory.CategoryType.GENERAL.getCode();
        }
        try {
            return EnumCode.ChatHistory.CategoryType.getCodeByValue(cate);
        } catch (IllegalArgumentException ex) {
            return EnumCode.ChatHistory.CategoryType.GENERAL.getCode();
        }
    }
}