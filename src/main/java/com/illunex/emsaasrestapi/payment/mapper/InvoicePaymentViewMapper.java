package com.illunex.emsaasrestapi.payment.mapper;

import com.illunex.emsaasrestapi.payment.vo.InvoicePaymentView;
import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

@Mapper
public interface InvoicePaymentViewMapper {
    InvoicePaymentView selectInvoicePaymentSummary(Integer invoiceId);

    Map<String, Object> selectDefaultMethodAndMandate(Integer partnershipIdx);
}
