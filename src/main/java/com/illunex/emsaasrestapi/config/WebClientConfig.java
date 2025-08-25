package com.illunex.emsaasrestapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class WebClientConfig {
    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        HttpClient http =
                reactor.netty.http.client.HttpClient.create()
                        .compress(false)
                        .keepAlive(true)
                        .responseTimeout(Duration.ofMinutes(10))
                        .doOnConnected(conn -> {
                        });

        return builder.clientConnector(new ReactorClientHttpConnector(http))
                .build();
    }
}
