package com.illunex.emsaasrestapi.payment.dto;

import com.illunex.emsaasrestapi.common.code.EnumCode;
import com.illunex.emsaasrestapi.payment.vo.InvoiceVO;
import com.illunex.emsaasrestapi.payment.vo.PaymentAttemptVO;
import lombok.*;
import org.springframework.cglib.core.Local;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

public class ResponsePaymentDTO {

    @Getter
    @Setter
    public static class RegisterPaymentMethod {
        private Integer paymentMethodIdx;
        private String method;
        private Map<String, Object> card;
        private String requestedAt;
    }


    @Getter
    @Setter
    public static class PaymentPreview {
        private Integer partnershipIdx;
        private Integer licensePartnershipIdx;
        private PreviewPlan currentPlan;
        private PreviewPlan targetPlan;
        private Integer chargeSeats;
        private LocalDate periodStart;
        private LocalDate periodEnd;
        private Integer denominatorDays;
        private String occurredAt;
        private String roundingRule;
        private String currency;
        private List<PreviewItem> items;
        private Long subTotal;
        private Long tax;
        private Long total;
        private boolean willChargeNow;
        private Long creditCarryOver;
        private List<String> notes;
    }

    @Setter
    @Getter
    public static class PreviewItem {
        private String itemType;       // RECURRING/PRORATION/CREDIT/ADJUSTMENT
        private String description;
        private Integer quantity;      // seats
        private Long unitPrice;
        private Integer days;          // numerator
        private Long amount;           // +/-
        private Long relatedEventId;   // null 가능
        private Map<String, Object> meta;
    }

    @Getter
    @Setter
    public static class PreviewPlan {
        private Integer idx;
        private String planCd;
        private String planCdDesc;
        private String name;
        private Integer pricePerUser;
        private Integer minUserCount;
        private Integer dataTotalLimit;
        private Integer projectCountLimit;

        public void setPlanCd(String planCd) {
            this.planCd = planCd;
            this.planCdDesc = EnumCode.getCodeDesc(planCdDesc);
        }
    }


    @Getter
    @Setter
    public static class LicenseChangeResult {
        private String orderId;
        private String orderName;
        private Integer amount;
        private PreviewPlan fromPlan;
        private PreviewPlan toPlan;
        private LicenseChangeStatus licenseChangeStatueStatus;
        private String status;
        private String errorCode;
        private String errorMessage;

        public enum LicenseChangeStatus {
            UPGRADE, DOWNGRADE, CANCEL
        }
    }


}
