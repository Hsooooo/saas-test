package com.illunex.emsaasrestapi.payment.vo;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.time.LocalDate;
import java.time.ZonedDateTime;

@Getter
@Setter
@Alias("InvoiceListViewVO")
public class InvoiceListViewVO {
    private Integer invoiceIdx;
    private ZonedDateTime payDate;
    private String planName;
    private Integer seatCount;
    private Integer amount;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private String displayState;
    private String receiptUrl;
}
