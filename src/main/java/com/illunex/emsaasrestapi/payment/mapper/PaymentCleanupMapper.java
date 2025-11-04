package com.illunex.emsaasrestapi.payment.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PaymentCleanupMapper {
    void deleteLicensePaymentHistoryByPartnership(@Param("partnershipIdx") Integer partnershipIdx);
    void deleteInvoiceItemsByPartnership(@Param("partnershipIdx") Integer partnershipIdx);
    void deletePaymentAttemptsByPartnership(@Param("partnershipIdx") Integer partnershipIdx);
    void deleteInvoicesByPartnership(@Param("partnershipIdx") Integer partnershipIdx);
    void deleteSubscriptionEventsByPartnership(@Param("partnershipIdx") Integer partnershipIdx);
    void deleteLicensePartnershipsByPartnership(@Param("partnershipIdx") Integer partnershipIdx);
    void deletePaymentMandatesByPartnership(@Param("partnershipIdx") Integer partnershipIdx);
    void deletePaymentMethodsByPartnership(@Param("partnershipIdx") Integer partnershipIdx);
}