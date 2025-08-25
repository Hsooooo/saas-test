package com.illunex.emsaasrestapi.config;

import com.illunex.emsaasrestapi.common.jwt.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsUtils;

@EnableWebFluxSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    public static final String MEMBER = "MEMBER";
    private final TokenProvider tokenProvider;
    private final JwtWebFilter jwtWebFilter;
    private final JwtAuthEntryPoint entryPoint;
    private final JwtAccessDenied accessDenied;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(entryPoint)
                        .accessDeniedHandler(accessDenied))
                .authorizeExchange(ex -> ex
                        // Preflight
                        .pathMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()

                        // 공개 경로
                        .pathMatchers("/").permitAll()
                        .pathMatchers("/member/**").permitAll()
                        .pathMatchers("/cert/**").permitAll()

                        // 권한 필요한 경로 (기존 규칙 그대로)
                        .pathMatchers("/member/mypage/**").hasAuthority(MEMBER)
                        .pathMatchers("/project/**").hasAuthority(MEMBER)
                        .pathMatchers("/project/category/**").hasAuthority(MEMBER)
                        .pathMatchers("/network/**").hasAuthority(MEMBER)
                        .pathMatchers("/query/**").hasAuthority(MEMBER)

                        // SSE 프록시
                        .pathMatchers("/ai/gpt/**").authenticated()

                        // 나머지는 인증만
                        .anyExchange().authenticated()
                )
                .securityContextRepository(org.springframework.security.web.server.context.NoOpServerSecurityContextRepository.getInstance())
                .addFilterAt(jwtWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }
}