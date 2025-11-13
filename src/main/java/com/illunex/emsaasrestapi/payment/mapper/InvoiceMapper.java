package com.illunex.emsaasrestapi.payment.mapper;

import com.illunex.emsaasrestapi.payment.vo.InvoiceVO;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Mapper
public interface InvoiceMapper {
    void insertByInvoiceVO(InvoiceVO inv);
    InvoiceVO selectActiveByPeriod(Integer lpIdx, LocalDate periodStart, LocalDate periodEnd);
    int recalcTotals(Integer invoiceIdx, BigDecimal vatRate);
    int markOpen(Integer invoiceIdx);
    InvoiceVO selectByIdx(Integer idx);
    Optional<InvoiceVO> selectLastIssuedByLicensePartnershipIdx(Integer licensePartnershipIdx);
    void markPaid(Integer invoiceIdx);
    void updateByInvoiceVO(InvoiceVO inv);

    InvoiceVO selectLastPaidByLicensePartnershipIdx(Integer idx);
}
