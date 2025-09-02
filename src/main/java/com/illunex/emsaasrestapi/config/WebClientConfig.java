package com.illunex.emsaasrestapi.config;

import io.netty.handler.logging.LogLevel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.logging.AdvancedByteBufFormat;

import java.time.Duration;

@Configuration
public class WebClientConfig {
    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        HttpClient http = HttpClient.create()
                .compress(false)                 // 압축 해제(조각 단위 파싱에 유리)
                .keepAlive(true)
                .responseTimeout(Duration.ofMinutes(10))
                // 디버깅 시 아래 wiretap 주석 해제하면 요청/응답 헤더/바디가 찍힘
                .wiretap("reactor.netty.http.client.HttpClient",
                        LogLevel.DEBUG, AdvancedByteBufFormat.TEXTUAL);

        return builder
                .clientConnector(new ReactorClientHttpConnector(http))
                .codecs(c -> {
                    // ❗ 256KB 기본 제한 상향(방어선). 우리는 청크로 직접 자르지만 혹시 모를 경로 대비
                    c.defaultCodecs().maxInMemorySize(10 * 1024 * 1024); // 10MB
                    // 필요하면: c.defaultCodecs().enableLoggingRequestDetails(true);
                })
                // .defaultHeader(HttpHeaders.ACCEPT, MediaType.ALL_VALUE)
                // ↑ 기본 Accept를 */*로 깔아도 되지만, 난 "요청부에서 명시"를 추천
                .build();
    }
}
