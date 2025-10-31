package com.illunex.emsaasrestapi.payment.vo;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

@Setter
@Getter
@Alias("PaymentMethodViewVO")
public class PaymentMethodViewVO {
    private Integer partnershipIdx;
    private Integer paymentMethodIdx;
    private Integer recentMandateIdx;
    private String customerKey;
    private Boolean isDefaultMethod;
}
