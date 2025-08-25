package com.illunex.emsaasrestapi.common.jwt;

import com.illunex.emsaasrestapi.common.CustomAuthException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class JwtWebFilter implements WebFilter {
    private final TokenProvider tokenProvider;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        var req = exchange.getRequest();
        var res = exchange.getResponse();
        String auth = req.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (auth != null && auth.startsWith("Bearer ")) {
            String jwt = auth.substring(7);
            try {
                tokenProvider.validateTokenAndThrow(jwt);
                var authentication = tokenProvider.getAuthentication(jwt);
                // Reactive SecurityContext에 심기
                return chain.filter(exchange)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
            } catch (CustomAuthException e) {
                res.setStatusCode(e.getHttpStatus());
                res.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                var bytes = ("{\"message\":\"" + e.getMessage() + "\"}").getBytes(StandardCharsets.UTF_8);
                return res.writeWith(Mono.just(res.bufferFactory().wrap(bytes)));
            }
        }
        return chain.filter(exchange);
    }

}
