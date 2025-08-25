package com.illunex.emsaasrestapi.chat;

import com.illunex.emsaasrestapi.chat.util.OpenAiSseParser;
import com.illunex.emsaasrestapi.common.CurrentMember;
import com.illunex.emsaasrestapi.common.code.EnumCode;
import com.illunex.emsaasrestapi.member.vo.MemberVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;
import reactor.core.scheduler.Schedulers;
import org.springframework.core.io.buffer.DataBuffer;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class AiProxyController {

    @Value("${ai.url}") String aiGptBase; // 예: https://api.openai.com
    private final WebClient webClient;        // WebClient.Builder 주입 권장
    private final ChatService chatService;    // 너가 만든 DB 저장 서비스

    // ✅ SSE 프록시 (바디 없음, 쿼리스트링만 전달)
    @RequestMapping(value = "ai/gpt/**", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Mono<Void> proxy(ServerWebExchange exchange,
                            @CurrentMember MemberVO memberVO,
                            @RequestParam("partnershipMemberIdx") Integer partnershipMemberIdx,
                            @RequestParam("query") String query,
                            @RequestParam(value = "title", required = false) String title,
                            @RequestParam(value = "user_id", required = false, defaultValue = "hi") String userId) {
        var req = exchange.getRequest();
        var res = exchange.getResponse();

        // 0) 방 resolve → USER 선저장
        final int chatRoomIdx = chatService.resolveChatRoom(partnershipMemberIdx, title);
        chatService.saveHistoryAsync(chatRoomIdx, EnumCode.ChatRoom.SenderType.USER.getCode(), query);

        var params = UriComponentsBuilder.fromUri(req.getURI()).build().getQueryParams();
        String queryParam = params.getFirst("query");

        URI target = UriComponentsBuilder.fromHttpUrl(aiGptBase)
                .path("/v2/api/report-generate")
                .queryParam("user_id", userId)
                .queryParam("query", queryParam)
                .build(true)
                .toUri();

        var upstream = webClient.post()
                .uri(target)
                .headers(h -> {
                    h.setAccept(List.of(MediaType.TEXT_EVENT_STREAM));
                    h.remove(HttpHeaders.CONTENT_TYPE);              // 빈 바디
                    h.add("X-Accel-Buffering", "no");
                })
                .body(Mono.empty(), Void.class)
                .httpRequest(r -> r.getHeaders().setContentLength(0))
                .exchangeToFlux(respec -> {
                    res.setStatusCode(respec.statusCode());
                    res.getHeaders().setContentType(MediaType.TEXT_EVENT_STREAM);
                    res.getHeaders().add("Cache-Control", "no-cache");
                    res.getHeaders().add("Connection", "keep-alive");
                    return respec.bodyToFlux(DataBuffer.class);
                });

        // ❗ 바이트로 누적 (문자열 변환 금지)
        final java.io.ByteArrayOutputStream teeBytes = new java.io.ByteArrayOutputStream(16 * 1024);

        return res.writeAndFlushWith(
                upstream
                        .doOnNext(db -> {
                            // DataBuffer 내용을 복사하되, 원본 readIndex는 건드리지 않음
                            java.nio.ByteBuffer bb = db.asByteBuffer().asReadOnlyBuffer();
                            byte[] copy = new byte[bb.remaining()];
                            bb.get(copy);
                            teeBytes.write(copy, 0, copy.length);
                        })
                        .doOnComplete(() -> {
                            String all = new String(teeBytes.toByteArray(), java.nio.charset.StandardCharsets.UTF_8);
                            String last = OpenAiSseParser.extractLastMessageFromSequence(all);
                            chatService.saveHistoryAsync(
                                    chatRoomIdx,
                                    EnumCode.ChatRoom.SenderType.ASSISTANT.getCode(),
                                    last.isBlank() ? all : last
                            );
                        })
                        .doOnError(e -> {
                            String all = new String(teeBytes.toByteArray(), java.nio.charset.StandardCharsets.UTF_8);
                            String last = OpenAiSseParser.extractLastMessageFromSequence(all);
                            chatService.saveHistoryAsync(
                                    chatRoomIdx,
                                    EnumCode.ChatRoom.SenderType.ASSISTANT.getCode(),
                                    last.isBlank() ? all : last
                            );
                        })
                        .map(Mono::just)
        );
    }

    private void copyFewHeaders(HttpHeaders in, HttpHeaders out) {
        for (String k : List.of("Accept", "Accept-Language", "User-Agent")) {
            if (in.containsKey(k)) out.put(k, in.get(k));
        }
    }
}