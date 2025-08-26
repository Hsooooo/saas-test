package com.illunex.emsaasrestapi.common.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.illunex.emsaasrestapi.common.CustomAuthException;
import com.illunex.emsaasrestapi.common.CustomResponse;
import com.illunex.emsaasrestapi.common.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

/**
 * JWT utility class. filters incoming requests and changes the currently authenticated principal if an authorization header is verified.
 */
public class JwtFilter extends GenericFilterBean {
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String AUTHORIZATION_SCHEME_BEARER = "Bearer ";

    private final TokenProvider tokenProvider;

    public JwtFilter(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        String jwt = resolveToken(httpServletRequest);
        try {
            if (StringUtils.hasText(jwt)) {
                tokenProvider.validateTokenAndThrow(jwt);     // 유효할 땐 그대로 진행
                Authentication auth = tokenProvider.getAuthentication(jwt);
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
            chain.doFilter(request, response);
        } catch (CustomAuthException ex) {                    // 인증 실패 → 여기서 바로 응답
            ErrorCode code = ex.getErrorCode();
            httpServletResponse.setStatus(ex.getHttpStatus().value());
            httpServletResponse.setContentType("application/json;charset=UTF-8");
            httpServletResponse.getWriter().write(
                    new ObjectMapper().writeValueAsString(
                            CustomResponse.builder()
                                    .status(code.getStatus())
                                    .data(code.getMessage())
                                    .message(ex.getMessage())
                                    .build()));
        }
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(AUTHORIZATION_SCHEME_BEARER)) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
