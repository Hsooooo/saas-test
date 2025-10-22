package com.illunex.emsaasrestapi.member.dto;


import lombok.*;

import java.time.ZonedDateTime;

public class ResponseMemberDTO {
    /**
     * 약관 정보
     */
    @Getter
    @Setter
    public static class Term {
        private Integer idx;
        private String subject;
        private String content;
        private Boolean required;
        private ZonedDateTime updateDate;
        private ZonedDateTime createDate;
    }

    /**
     * 로그인 정보
     */
    @Getter
    @Setter
    public static class Login {
        private String email;
        private String accessToken;
        private Member member;
    }

    /**
     * 회원 정보
     */
    @Getter
    @Setter
    public static class Member {
        private Integer idx;
        private String email;
        private String name;
    }
}
