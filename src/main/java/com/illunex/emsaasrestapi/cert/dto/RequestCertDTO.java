package com.illunex.emsaasrestapi.cert.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class RequestCertDTO {

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Cert {
        private String certData;
    }

    @Setter
    @Getter
    @NoArgsConstructor
    public static class InviteSignup {
        // 이메일 초대링크 url or 링크초대 url
        private String certData;
        private String password;
        private String name;
        // 링크로 초대한 경우 필수
        private String email;
    }
}
