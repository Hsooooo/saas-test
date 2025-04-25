package com.illunex.emsaasrestapi.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 현재 로그인된 사용자를 컨트롤러의 파라미터에 자동 주입하기 위한 어노테이션
 * <p>
 * 내부적으로 Spring MVC의 HandlerMethodArgumentResolver인
 * {@link com.illunex.emsaasrestapi.common.CurrentMemberArgumentResolver} 를 통해 처리
 * <p>
 * SecurityContextHolder에서 인증 정보를 추출하여 {@link com.illunex.emsaasrestapi.member.vo.MemberVO}로 변환
 * <p>
 * 사용 예시:
 *     public ResponseEntity<?> getInfo(@CurrentMember MemberVO member) { ... }
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentMember {
}
