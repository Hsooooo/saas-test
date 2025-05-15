package com.illunex.emsaasrestapi.cert.dto;

import lombok.Getter;
import lombok.Setter;

public class RequestCertDTO {

    @Getter
    @Setter
    public static class Cert {
        private String certData;
    }

    @Getter
    @Setter
    public static class InviteSignup {
        private String certData;
        private String password;
    }
}
