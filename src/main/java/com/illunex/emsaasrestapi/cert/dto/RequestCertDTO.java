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
        private String certData;
        private String password;
    }
}
