package com.illunex.emsaasrestapi.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
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
    JWT_TOKEN_MISSING(103, "인증 토큰이 없습니다. 다시 로그인해주세요."),
    JWT_TOKEN_EXPIRED(104, "로그인 세션이 만료되었습니다. 다시 로그인해주세요."),
    JWT_TOKEN_MALFORMED(105, "토큰 형식이 잘못되었습니다. 다시 로그인해주세요."),
    JWT_TOKEN_INVALID_SIGNATURE(106, "위조된 토큰입니다. 다시 로그인해주세요."),
    COMMON_NOT_FOUND_FILE(801, "파일을 찾을 수 없습니다."),
    COMMON_EMAIL_CERTIFICATE_EXPIRE(107, "인증이 만료 되었습니다."),
    COMMON_EMAIL_CERTIFICATE_INVALID(108, "인증이 유효 하지 않습니다."),
    COMMON_ALREADY_EMAIL_CERTIFICATE(109, "이미 인증이 완료된 계정입니다."),

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
    MEMBER_REG_PASSWORD(310, "비밀번호는 8자리 이상 16자리 이하의 영문 대문자, 영문 소문자, 숫자, 특수문자를 포함하여 입력해주세요."),
    MEMBER_EMPTY_PASSWORD(311, "비밀번호를 입력해주세요"),

    /**
     * 파트너쉽
     */
    PARTNERSHIP_DOMAIN_DUPLICATE(401, "중복되는 도메인입니다."),
    PARTNERSHIP_INVALID_MEMBER(402, "유효한 파트너쉽 회원이 아닙니다."),
    PARTNERSHIP_MEMBER_DUPLICATE_EMAIL(403, "동일한 이메일로 초대된 회원이 있습니다."),

    /**
     * 프로젝트
     */
    PROJECT_INVALID_FILE_EXTENSION(500, "지원되지 않는 확장자 입니다."),
    PROJECT_INVALID_FILE_DATA_ROW_EMPTY(501, "행의 데이터가 없습니다."),
    PROJECT_INVALID_FILE_DATA_COLUMN_EMPTY(502, "열의 데이터가 없습니다."),
    PROJECT_NOT_FOUND(503, "프로젝트 정보가 없습니다."),
    PROJECT_DELETED(504, "삭제된 프로젝트 입니다."),
    PROJECT_EMPTY_DATA(505, "저장된 데이터가 없습니다."),
    PROJECT_CONTENT_EMPTY_DATA(505, "저장된 설정 데이터가 없습니다."),
    PROJECT_INVALID_MEMBER(506, "유효한 프로젝트 구성원이 아닙니다."),

    PROJECT_CATEGORY_INVALID_SORT_ORDER(550, "요청된 정렬 순서가 올바르지 않습니다."),
    ;

    private final int status;
    private final String message;
}
