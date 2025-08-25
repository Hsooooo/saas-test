package com.illunex.emsaasrestapi.member;

import com.illunex.emsaasrestapi.common.CustomException;
import com.illunex.emsaasrestapi.common.code.BaseCodeEnum;
import com.illunex.emsaasrestapi.common.code.EnumCode;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Objects;

import static com.illunex.emsaasrestapi.common.code.EnumCode.Member.StateCd.*;

@Component
public class MemberComponent {

    /** 회원 승인 상태 체크 (그대로 사용) */
    public void checkMemberState(String stateCd) throws CustomException {
        switch (Objects.requireNonNull(BaseCodeEnum.fromCode(EnumCode.Member.StateCd.class, stateCd))) {
            case Wait       -> throw new CustomException(com.illunex.emsaasrestapi.common.ErrorCode.MEMBER_STATE_WAIT);
            case Suspend    -> throw new CustomException(com.illunex.emsaasrestapi.common.ErrorCode.MEMBER_STATE_SUSPEND);
            case Withdrawal -> throw new CustomException(com.illunex.emsaasrestapi.common.ErrorCode.MEMBER_STATE_WITHDRAWAL);
            case Approval   -> { /* ok */ }
            default         -> throw new CustomException(com.illunex.emsaasrestapi.common.ErrorCode.COMMON_INTERNAL_SERVER_ERROR);
        }
    }

    /* =========================
       WebFlux용 클라이언트 정보 추출
       ========================= */

    /** IP 추출 (권장: ServerWebExchange에서 호출) */
    public String getClientIpAddr(ServerWebExchange exchange) {
        return getClientIpAddr(exchange.getRequest());
    }

    /** IP 추출 (ServerHttpRequest) */
    public String getClientIpAddr(ServerHttpRequest request) {
        HttpHeaders h = request.getHeaders();

        // 1) X-Forwarded-For: "client, proxy1, proxy2"
        String xff = firstNonEmpty(
                h.getFirst("X-Forwarded-For"),
                h.getFirst("X_FORWARDED_FOR")
        );
        String ip = extractFirstIp(xff);
        if (notEmpty(ip)) return ip;

        // 2) X-Real-IP
        ip = firstNonEmpty(h.getFirst("X-Real-IP"), h.getFirst("X_REAL_IP"));
        if (notEmpty(ip)) return ip;

        // 3) 기타 레거시 헤더들
        ip = firstNonEmpty(
                h.getFirst("Proxy-Client-IP"),
                h.getFirst("WL-Proxy-Client-IP"),
                h.getFirst("HTTP_CLIENT_IP"),
                h.getFirst("HTTP_X_FORWARDED_FOR")
        );
        ip = extractFirstIp(ip);
        if (notEmpty(ip)) return ip;

        // 4) Remote Address (reactor-netty)
        InetSocketAddress remote = request.getRemoteAddress();
        if (remote != null && remote.getAddress() != null) {
            return remote.getAddress().getHostAddress();
        }
        return "unknown";
    }

    /** 브라우저 식별 (간단판, 기존 로직 유지) */
    public String getClientPlatform(ServerWebExchange exchange) {
        return getClientPlatform(exchange.getRequest());
    }
    public String getClientPlatform(ServerHttpRequest request) {
        String ua = request.getHeaders().getFirst("User-Agent");
        if (ua == null) return "";
        if (ua.contains("Trident")) return "ie";
        if (ua.contains("Edge"))    return "edge";
        if (ua.contains("Whale"))   return "whale";
        if (ua.contains("Opera") || ua.contains("OPR")) return "opera";
        if (ua.contains("Firefox")) return "firefox";
        if (ua.contains("Safari") && !ua.contains("Chrome")) return "safari";
        if (ua.contains("Chrome"))  return "chrome";
        return "";
    }

    /** 디바이스/플랫폼 문자열 (UA 원문) */
    public String getClientDevice(ServerWebExchange exchange) {
        return getClientDevice(exchange.getRequest());
    }
    public String getClientDevice(ServerHttpRequest request) {
        String ua = request.getHeaders().getFirst("User-Agent");
        return ua != null ? ua : "";
    }

    /* ===========
       헬퍼 메서드
       =========== */
    private static String firstNonEmpty(String... vals) {
        if (vals == null) return null;
        return Arrays.stream(vals)
                .filter(v -> v != null && !v.isBlank() && !"unknown".equalsIgnoreCase(v))
                .findFirst().orElse(null);
    }

    private static boolean notEmpty(String s) { return s != null && !s.isBlank(); }

    /** XFF에서 첫 번째 IP만 깔끔히 추출 (공백 제거) */
    private static String extractFirstIp(String value) {
        if (value == null || value.isBlank()) return null;
        String first = value.split(",")[0].trim();
        // IPv6: "2001:db8::1" 그대로 반환 (포트가 붙었다면 제거)
        int portIdx = first.indexOf(':');
        // 주의: IPv6 주소 자체에 콜론 포함 → 대괄호가 없을 땐 포트 분리하지 않음
        if (first.startsWith("[") && first.contains("]")) { // [::1]:port
            int end = first.indexOf(']');
            if (end > 0) {
                String host = first.substring(1, end);
                return host;
            }
        }
        // IPv4: 1.2.3.4:5678 → 1.2.3.4
        if (first.contains(".") && portIdx > -1) {
            return first.substring(0, portIdx);
        }
        return first;
    }
}
