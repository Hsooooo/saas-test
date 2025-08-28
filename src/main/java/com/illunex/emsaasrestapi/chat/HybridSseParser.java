package com.illunex.emsaasrestapi.chat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.illunex.emsaasrestapi.common.code.EnumCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
@RequiredArgsConstructor
public class HybridSseParser {
    private final ObjectMapper om;
    private static final String KEY_PHRASE = "외부 데이터를 가져오는 중"; // 포함 매칭
    private static final String KEY_TOOL_PHRASE = "get_search_result_by_query_tools";

    public Flux<SseEvent> parse(Flux<String> chunks) {
        return Flux.create(sink -> {
            final StringBuilder sseBuf = new StringBuilder(); // SSE 블록 버퍼
            final StringBuilder rawBuf = new StringBuilder(); // RAW JSON 누적 버퍼
            final java.util.concurrent.atomic.AtomicBoolean sseMode = new java.util.concurrent.atomic.AtomicBoolean(false);

            chunks.subscribe(chunk -> {
                // 한번이라도 data:/event: 보이면 SSE 모드 ON
                if (!sseMode.get() && (chunk.contains("data:") || chunk.contains("event:"))) {
                    sseMode.set(true);
                }

                if (!sseMode.get()) {
                    // RAW 모드: 라인/청크에 섞인 순수 JSON 오브젝트를 뽑는다
                    rawBuf.append(chunk);
                    for (String json : drainBalancedJsonObjects(rawBuf)) {
                        SseEvent ev = toToolProgressIfMatch(json);
                        if (ev != null) sink.next(ev);
                    }
                    // 타임스탬프 같은 잡음 라인은 자동 무시 (JSON만 본다)
                    return;
                }

                // SSE 모드: \n\n 또는 \r\n\r\n 기준으로 블록 분리
                sseBuf.append(chunk);
                int idx;
                while ((idx = indexOfDoubleNewline(sseBuf)) >= 0) {
                    int sepLen = doubleNewlineLen(sseBuf, idx);
                    String block = sseBuf.substring(0, idx);
                    sseBuf.delete(0, idx + sepLen);

                    // data: 라인 합치기
                    String dataJson = block.lines()
                            .map(String::stripLeading)
                            .filter(l -> l.startsWith("data:"))
                            .map(l -> l.substring(5).trim())
                            .reduce("", String::concat);
                    if (dataJson.isBlank()) continue;

                    SseEvent ev = toToolProgressIfMatch(dataJson);
                    if (ev != null) sink.next(ev);
                }
            }, sink::error, () -> {
                // 남은 RAW/SSE 버퍼 마무리
                for (String json : drainBalancedJsonObjects(rawBuf)) {
                    SseEvent ev = toToolProgressIfMatch(json);
                    if (ev != null) sink.next(ev);
                }
                if (sseBuf.length() > 0) {
                    String block = sseBuf.toString();
                    String dataJson = block.lines()
                            .map(String::stripLeading)
                            .filter(l -> l.startsWith("data:"))
                            .map(l -> l.substring(5).trim())
                            .reduce("", String::concat);
                    if (!dataJson.isBlank()) {
                        SseEvent ev = toToolProgressIfMatch(dataJson);
                        if (ev != null) sink.next(ev);
                    }
                }
                sink.complete();
            });
        });
    }

    // ----- helpers -----

    /** RAW 모드에서 완결 JSON 오브젝트들을 뽑아냄 (문자열/이스케이프 안전) */
    private java.util.List<String> drainBalancedJsonObjects(StringBuilder buf) {
        java.util.ArrayList<String> out = new java.util.ArrayList<>();
        int depth = 0, start = -1;
        boolean inStr = false, esc = false;

        for (int i = 0; i < buf.length(); i++) {
            char c = buf.charAt(i);
            if (inStr) {
                if (esc) { esc = false; continue; }
                if (c == '\\') { esc = true; continue; }
                if (c == '"') inStr = false;
                continue;
            }
            if (c == '"') { inStr = true; continue; }
            if (c == '{') {
                if (depth == 0) start = i;
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0 && start >= 0) {
                    out.add(buf.substring(start, i + 1));
                    start = -1;
                }
            }
        }
        // 남은 꼬다리는 buf에 유지
        if (!out.isEmpty()) {
            int lastEnd = buf.lastIndexOf("}");
            if (lastEnd >= 0) buf.delete(0, lastEnd + 1);
        }
        return out;
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

    /** status 매칭되면 ToolProgress, 아니면 null */
    private SseEvent toToolProgressIfMatch(String json) {
        try {
            var node = om.readTree(json);
            String status = findLoose(node);
            String toolNode = findLooseTool(node);
            String enumCode = EnumCode.ChatToolResult.ToolType.QUERY_RESULT.getValue();

            if (status != null && status.contains(KEY_PHRASE)
                    && eqNorm(toolNode, enumCode)) {
                String tool = node.path("tool").asText();
                if (tool == null || tool.isBlank()) tool = "unknown_tool";
                return new SseEvent.ToolProgress(tool, json);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private String findLoose(com.fasterxml.jackson.databind.JsonNode n) {
        // status 또는 유사 필드 몇 군데 느슨 탐색
        String direct = text(n, "status"); if (direct != null) return direct;
        for (String k : new String[]{"content.status", "data.status", "meta.status", "detail.status", "message", "desc"}) {
            String v = dotted(n, k);
            if (v != null) return v;
        }
        return null;
    }

    private String findLooseTool(JsonNode node) {
        // 가장 먼저 tool 필드 텍스트만 본다
        JsonNode t = node.path("tool");
        if (t.isTextual()) return t.asText();

        // 그래도 없으면 흔히 오는 nested 위치들 느슨 탐색
        for (String dotted : new String[]{"data.tool", "meta.tool", "detail.tool"}) {
            JsonNode cur = node;
            for (String k : dotted.split("\\.")) cur = cur.path(k);
            if (cur.isTextual()) return cur.asText();
        }
        return null;
    }

    private String text(com.fasterxml.jackson.databind.JsonNode n, String key) {
        var v = n.path(key);
        return v.isTextual() ? v.asText() : null;
    }
    private String dotted(com.fasterxml.jackson.databind.JsonNode n, String dotted) {
        var cur = n;
        for (String k : dotted.split("\\.")) cur = cur.path(k);
        return cur.isTextual() ? cur.asText() : null;
    }

    private static String norm(String s) {
        if (s == null) return null;
        // 유니코드 NFKC 정규화 + trim + 양끝 따옴표 제거
        String n = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFKC).trim();
        if (n.length() >= 2 && n.charAt(0) == '"' && n.charAt(n.length()-1) == '"') {
            n = n.substring(1, n.length()-1);
        }
        return n;
    }

    private static boolean eqNorm(String a, String b) {
        a = norm(a); b = norm(b);
        return a != null && b != null && a.equals(b);
    }
}