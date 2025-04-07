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
        private String required;
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
    }

    /**
     * 회원 정보
     */
    @Getter
    @Setter
    public static class Member {
        private String email;
        private String nickname;
        private String profileImageUrl;
        private String comment;
    }
}
