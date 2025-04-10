package com.illunex.emsaasrestapi.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum ErrorCode {

    OK(200, "성공"),

    /**
     * 공용
     */
    COMMON_EMPTY(100, "데이터가 없습니다."),
    COMMON_INVALID(101, "잘못된 요청입니다."),
    COMMON_FAIL_AUTHENTICATION(102, "권한이 없습니다."),
    COMMON_INTERNAL_SERVER_ERROR(500, "서버 에러"),
    COMMON_INVALID_FILE_EXTENSION(800, "잘못된 형식의 파일 입니다."),
    COMMON_NOT_FOUND_FILE(801, "파일을 찾을 수 없습니다."),

    /**
     * 회원
     */
    MEMBER_EMPTY_ACCOUNT(300, "아이디가 없습니다."),
    MEMBER_NOT_MATCH_PASSWORD(301, "비밀번호가 다릅니다."),
    MEMBER_ALREADY_EXISTS(302, "가입된 이메일이 있습니다."),
    MEMBER_STATE_WAIT(303, "메일 인증되지 않았습니다. 메일 인증을 진행해주세요."),
    MEMBER_STATE_SUSPEND(304, "정지된 회원입니다. 관리자에게 문의해주세요."),
    MEMBER_STATE_WITHDRAWAL(305, "탈퇴한 회원입니다. 재가입 후 로그인 해주세요."),
    MEMBER_EMAIL_CERTIFICATE_EXPIRE(306, "인증이 만료 되었습니다."),
    MEMBER_EMAIL_CERTIFICATE_INVALID(307, "인증이 유효 하지 않습니다."),
    MEMBER_NICKNAME_DUPLICATE(308, "중복된 닉네임이 있습니다."),
    MEMBER_ALREADY_EMAIL_CERTIFICATE(309, "이미 인증이 완료된 계정입니다."),

    /**
     * 파트너쉽
     */
    PARTNERSHIP_DOMAIN_DUPLICATE(401, "중복되는 도메인입니다."),;

    @Getter
    private int status;
    @Getter
    private String message;
}
