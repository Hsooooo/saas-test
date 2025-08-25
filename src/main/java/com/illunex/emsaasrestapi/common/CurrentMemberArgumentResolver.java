package com.illunex.emsaasrestapi.common;

import org.springframework.security.core.context.ReactiveSecurityContextHolder;

import com.illunex.emsaasrestapi.member.MemberService;
import com.illunex.emsaasrestapi.member.vo.MemberVO;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.BindingContext;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class CurrentMemberArgumentResolver implements org.springframework.web.reactive.result.method.HandlerMethodArgumentResolver {
    private final MemberService memberService;

    public CurrentMemberArgumentResolver(MemberService memberService) {
        this.memberService = memberService;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentMember.class)
                && parameter.getParameterType().equals(MemberVO.class);
    }

    @Override
    public Mono<Object> resolveArgument(MethodParameter parameter,
                                        BindingContext context,
                                        ServerWebExchange exchange) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(auth -> {
                    String email = ((User) auth.getPrincipal()).getUsername();
                    try {
                        return memberService.findByEmail(email); // 필요하면 Mono<MemberVO>로 래핑
                    } catch (CustomException e) {
                        throw new RuntimeException(e);
                    }
                });
    }
}
