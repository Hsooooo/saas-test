package com.illunex.emsaasrestapi.cert.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

public class RequestCertDTO {

    @Builder
    @Getter
    @Setter
    public static class Cert {
        private String certData;
    }

    @Builder
    @Setter
    @Getter
    public static class InviteSignup {
        private String certData;
        private String password;
    }
}
