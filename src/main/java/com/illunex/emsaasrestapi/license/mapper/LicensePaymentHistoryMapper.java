package com.illunex.emsaasrestapi.license.mapper;

import com.illunex.emsaasrestapi.license.vo.LicensePartnershipVO;
import com.illunex.emsaasrestapi.license.vo.LicensePaymentHistoryVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

@Mapper
public interface LicensePaymentHistoryMapper {
    void insertByLicensePaymentHistoryVO(LicensePaymentHistoryVO licensePaymentHistoryVO);
    Optional<LicensePaymentHistoryVO> selectByInvoiceIdxAndLastPaidDate(Integer invoiceIdx);
}
