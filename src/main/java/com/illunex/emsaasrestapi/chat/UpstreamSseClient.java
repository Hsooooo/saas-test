package com.illunex.emsaasrestapi.chat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class UpstreamSseClient {
    private final WebClient webClient;

    public Flux<String> stream(String baseUrl, String path, Map<String, Object> body) {
        return webClient.post()
                .uri(UriComponentsBuilder.fromHttpUrl(baseUrl).path(path).build().toUri())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.ALL)                    // ⬅️ text/event-stream 빼고 */* 로 받기 (SSE 리더 우회)
                .bodyValue(body)
                .exchangeToFlux(res -> res.bodyToFlux(org.springframework.core.io.buffer.DataBuffer.class))
                .map(db -> {
                    try {
                        byte[] bytes = new byte[db.readableByteCount()];
                        db.read(bytes);
                        return new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
                    } finally {
                        org.springframework.core.io.buffer.DataBufferUtils.release(db); // ⬅️ 반드시 release
                    }
                })
                .doOnSubscribe(s -> log.debug("[SSE] subscribe"))
                .doOnError(e -> log.error("[SSE] error", e))
                .doFinally(s -> log.debug("[SSE] finally: {}", s));
    }
}