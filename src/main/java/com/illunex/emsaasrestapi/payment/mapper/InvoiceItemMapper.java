package com.illunex.emsaasrestapi.payment.mapper;

import com.illunex.emsaasrestapi.payment.vo.InvoiceItemVO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface InvoiceItemMapper {
    void insertByInvoiceItemVO(InvoiceItemVO invoiceItemVO);

    InvoiceItemVO selectRecurringByInvoiceIdx(Integer invoiceIdx);
}
