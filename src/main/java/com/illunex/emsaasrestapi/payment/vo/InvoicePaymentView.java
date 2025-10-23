package com.illunex.emsaasrestapi.payment.vo;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.math.BigDecimal;

@Getter
@Setter
@Alias("InvoicePaymentView")
public class InvoicePaymentView {
    private Integer invoiceId;
    private BigDecimal invoiceTotal;
    private BigDecimal paidTotal;
    private BigDecimal balanceDue;
}
