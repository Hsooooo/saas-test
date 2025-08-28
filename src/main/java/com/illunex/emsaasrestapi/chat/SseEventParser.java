package com.illunex.emsaasrestapi.chat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class SseEventParser {
    private final ObjectMapper om;
    private static final String KEY_PHRASE = "외부 데이터를 가져오는 중";

    /** 청크 스트림 → 이벤트 스트림 */
    public Flux<SseEvent> parse(Flux<String> chunks) {
        return splitSseBlocks(chunks)
                .handle((block, sink) -> {
                    try {
                        SseEvent ev = toEventOrNull(block);
                        if (ev != null) sink.next(ev);
                    } catch (Exception e) {
                        // 파싱 실패는 조용히 스킵 (로그만)
                        if (log.isDebugEnabled()) log.debug("[SSE][parse] skip block: {}", e.toString());
                    }
                });
    }

    /** "\n\n" 또는 "\r\n\r\n" 기준으로 블록 분리 */
    private Flux<String> splitSseBlocks(Flux<String> chunks) {
        return Flux.create(sink -> {
            final StringBuilder buf = new StringBuilder();
            chunks.subscribe(
                    chunk -> {
                        buf.append(chunk);
                        int idx;
                        while ((idx = indexOfDoubleNewline(buf)) >= 0) {
                            int sepLen = doubleNewlineLen(buf, idx);
                            String block = buf.substring(0, idx);
                            buf.delete(0, idx + sepLen);
                            sink.next(block);
                        }
                    },
                    sink::error,
                    () -> { // 잔여 버퍼
                        if (buf.length() > 0) sink.next(buf.toString());
                        sink.complete();
                    }
            );
        });
    }

    private int indexOfDoubleNewline(CharSequence s) {
        String str = s.toString();
        int i = str.indexOf("\n\n");
        if (i >= 0) return i;
        return str.indexOf("\r\n\r\n");
    }
    private int doubleNewlineLen(CharSequence s, int pos) {
        String str = s.toString();
        return (pos + 3 < str.length() && str.startsWith("\r\n\r\n", pos)) ? 4 : 2;
    }

    /** 블록 → SseEvent (status/툴/assistant delta 느슨 매칭) */
    private SseEvent toEventOrNull(String block) {
        // data: 라인 모으기
        String dataJson = block.lines()
                .map(String::stripLeading)
                .filter(l -> l.startsWith("data:"))
                .map(l -> l.substring(5).trim())
                .reduce("", String::concat);
        if (dataJson.isBlank()) return null;

        try {
            var node = om.readTree(dataJson);

            // 1) 툴 진행 이벤트
            String status = findText(node, "status", "content.status", "data.status", "meta.status", "detail.status", "message", "desc");
            if (status != null && status.contains(KEY_PHRASE)) {
                String tool = findText(node, "tool");
                if (tool == null || tool.isBlank()) tool = "unknown_tool";
                return new SseEvent.ToolProgress(tool, om.writeValueAsString(node)); // payload 통짜 저장
            }

            // 2) assistant delta (선택) — 필요 시 토큰 누적에 이용
            String delta = findText(node, "delta", "text", "content");
            if (delta != null && !delta.isBlank()) {
                return new SseEvent.AssistantDelta(delta);
            }
            return null;
        } catch (Exception e) {
            // 파싱 실패는 그냥 스킵 (로그만)
            log.debug("[SSE][parse] skip block: {}", e.toString());
            return null;
        }
    }

    private String findText(JsonNode root, String... dottedKeys) {
        for (String key : dottedKeys) {
            JsonNode cur = root;
            for (String k : key.split("\\.")) {
                cur = cur.path(k);
            }
            if (cur.isTextual()) return cur.asText();
        }
        return null;
    }
}