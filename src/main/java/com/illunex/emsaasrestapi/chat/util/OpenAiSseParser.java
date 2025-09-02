package com.illunex.emsaasrestapi.chat.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;


public final class OpenAiSseParser {
    private static final ObjectMapper M = new ObjectMapper();

    /** JSON 객체들이 공백/개행으로 연달아 오는 텍스트에서 마지막 message만 뽑음 */
    public static String extractLastMessageFromSequence(String allText) {
        if (allText == null || allText.isBlank()) return "";
        String last = "";
        try {
            MappingIterator<JsonNode> it = M.readerFor(JsonNode.class).readValues(allText);
            while (it.hasNext()) {
                JsonNode n = it.next();
                if (n.hasNonNull("message") && n.get("message").isTextual()) {
                    last = n.get("message").asText("");
                }
            }
        } catch (Exception ignore) {
            // 만약 시퀀스 파싱이 실패하면 라인기반 백업
            last = extractLastMessageLineFallback(allText);
        }
        return last;
    }

    public static String extractLastCategoryFromSequence(String allText) {
        if (allText == null || allText.isBlank()) return "";
        String last = "";
        try {
            MappingIterator<JsonNode> it = M.readerFor(JsonNode.class).readValues(allText);
            while (it.hasNext()) {
                JsonNode n = it.next();
                if (n.hasNonNull("category") && n.get("category").isTextual()) {
                    last = n.get("category").asText("");
                }
            }
        } catch (Exception ignore) {
            // 만약 시퀀스 파싱이 실패하면 라인기반 백업
            last = extractLastMessageLineFallback(allText);
        }
        return last;
    }

    /** 라인 기반 백업 파서 (완성된 JSON 라인만 잡음) */
    private static String extractLastMessageLineFallback(String allText) {
        String last = "";
        for (String raw : allText.split("\n")) {
            if (raw == null) continue;
            String line = raw.strip();
            if (line.isEmpty() || line.startsWith(":")) continue;
            if (line.startsWith("data:")) line = line.substring(5).trim();
            if (!line.startsWith("{")) continue;
            try {
                JsonNode n = M.readTree(line);
                if (n.hasNonNull("message") && n.get("message").isTextual()) {
                    last = n.get("message").asText("");
                }
            } catch (Exception ignore) {}
        }
        return last;
    }

    private OpenAiSseParser() {}

    public static String extractLastPptxFromSequence(String all) {
        if (all == null || all.isBlank()) return "";
        String last = "";
        try {
            MappingIterator<JsonNode> it = M.readerFor(JsonNode.class).readValues(all);
            while (it.hasNext()) {
                JsonNode n = it.next();
                if (n.hasNonNull("pptx") && n.get("pptx").isTextual()) {
                    last = n.get("pptx").asText("");
                }
            }
        } catch (Exception ignore) {
            // 만약 시퀀스 파싱이 실패하면 라인기반 백업
            last = extractLastMessageLineFallback(all);
        }
        return last;
    }

    public static String extractLastDocsFromSequence(String all) {
        if (all == null || all.isBlank()) return "";
        String last = "";
        try {
            MappingIterator<JsonNode> it = M.readerFor(JsonNode.class).readValues(all);
            while (it.hasNext()) {
                JsonNode n = it.next();
                if (n.hasNonNull("docs") && n.get("docs").isTextual()) {
                    last = n.get("docs").asText("");
                }
            }
        } catch (Exception ignore) {
            // 만약 시퀀스 파싱이 실패하면 라인기반 백업
            last = extractLastMessageLineFallback(all);
        }
        return last;
    }
}
