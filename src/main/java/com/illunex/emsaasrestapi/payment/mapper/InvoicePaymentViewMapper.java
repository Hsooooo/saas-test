package com.illunex.emsaasrestapi.payment.mapper;

import com.illunex.emsaasrestapi.payment.dto.RequestPaymentDTO;
import com.illunex.emsaasrestapi.payment.vo.InvoiceListViewVO;
import com.illunex.emsaasrestapi.payment.vo.InvoicePaymentView;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

@Mapper
public interface InvoicePaymentViewMapper {
    InvoicePaymentView selectInvoicePaymentSummary(Integer invoiceId);

    Map<String, Object> selectDefaultMethodAndMandate(Integer partnershipIdx);

    List<InvoiceListViewVO> selectAllBySearchInvoiceAndPageable(RequestPaymentDTO.SearchInvoice searchInvoice, Pageable pageable);

    long countAllBySearchInvoice(RequestPaymentDTO.SearchInvoice searchInvoice);
}
