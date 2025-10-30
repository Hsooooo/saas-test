package com.illunex.emsaasrestapi.chat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
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
                .accept(MediaType.TEXT_EVENT_STREAM) // ⬅️ SSE 타입 명시
                .bodyValue(body)
                .exchangeToFlux(res -> {
                    if (res.statusCode().is2xxSuccessful()) {
                        return res.bodyToFlux(DataBuffer.class);
                    }
                    // 오류 본문을 문자열로 모아 예외로 방출
                    return res.bodyToFlux(DataBuffer.class)
                            .map(db -> {
                                byte[] tmp = new byte[db.readableByteCount()];
                                db.read(tmp);
                                DataBufferUtils.release(db);
                                return tmp;
                            })
                            .reduce(new ByteArrayOutputStream(), (baos, bytes) -> { try { baos.write(bytes); } catch (Exception ignore) {} return baos; })
                            .flatMapMany(baos -> Flux.error(new IllegalStateException("Upstream HTTP " + res.statusCode() + ": " + new String(baos.toByteArray(), StandardCharsets.UTF_8))));
                })
                .transform(UpstreamSseClient::decodeUtf8Incremental) // ⬅️ 증분 UTF-8 디코딩
                .doOnSubscribe(s -> log.debug("[SSE] subscribe"))
                .doOnError(e -> log.error("[SSE] error", e))
                .doFinally(s -> log.debug("[SSE] finally: {}", s));
    }

    /**
     * UTF-8 멀티바이트가 청크 경계에서 끊겨도 손실 없이 복구하는 증분 디코딩.
     * - 매 호출 사이 남은 바이트(carry)를 보존한다.
     * - 잘못된/미완성 시퀀스는 다음 청크에서 이어서 처리한다.
     */
    private static Flux<String> decodeUtf8Incremental(Flux<DataBuffer> in) {
        final CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPLACE)
                .onUnmappableCharacter(CodingErrorAction.REPLACE);

        final ByteArrayOutputStream carry = new ByteArrayOutputStream(4096);

        return in.map(db -> {
                    try {
                        byte[] chunk = new byte[db.readableByteCount()];
                        db.read(chunk);
                        // carry + chunk
                        carry.write(chunk);
                        byte[] bytes = carry.toByteArray();

                        ByteBuffer bb = ByteBuffer.wrap(bytes);
                        // 충분한 크기의 CharBuffer (최대 char/byte 비율 고려)
                        CharBuffer out = CharBuffer.allocate((int) (bytes.length * (double)decoder.maxCharsPerByte() + 4));

                        CoderResult cr = decoder.decode(bb, out, false);
                        out.flip();
                        String s = out.toString();

                        // 소비된 만큼 carry에서 제거하고 나머지(미완성 바이트) 보존
                        int consumed = bb.position();
                        if (consumed > 0) {
                            ByteArrayOutputStream next = new ByteArrayOutputStream();
                            next.write(bytes, consumed, bytes.length - consumed);
                            carry.reset();
                            next.writeTo(carry);
                        }
                        return s;
                    } catch (Exception e) {
                        // 디코딩 중 예외는 치명적 → 상위로 전달
                        throw new RuntimeException(e);
                    } finally {
                        DataBufferUtils.release(db);
                    }
                })
                .filter(s -> !s.isEmpty());
    }
}
