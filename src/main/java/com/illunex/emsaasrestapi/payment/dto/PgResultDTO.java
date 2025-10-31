package com.illunex.emsaasrestapi.payment.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PgResultDTO {
    private Boolean success;
    private String failureCode;
    private String failureMessage;
    private String orderId;

    public boolean isSuccess() {
        return Boolean.TRUE.equals(success);
    }
}
