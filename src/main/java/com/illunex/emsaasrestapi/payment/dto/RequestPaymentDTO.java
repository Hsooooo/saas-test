package com.illunex.emsaasrestapi.payment.dto;

import com.illunex.emsaasrestapi.partnership.dto.RequestPartnershipDTO;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class RequestPaymentDTO {
    @Getter
    public static class MethodRegister {
        private Integer partnershipIdx;
        private String customerKey;
        private String authKey;
        private String cardBrand;
        private String last4;
        private String expYear;
        private String expMonth;
    }

    @Getter
    public static class SubscriptionChangeEvent {
        private Integer partnershipIdx;
        private Integer licenseIdx;
        private String action;
        private String effective;
        private String orderNumber;
    }

    @Getter
    @Setter
    public static class SubscriptionInfo {
        private Integer partnershipIdx;
        private Integer licenseIdx;
        private String action;
        private String effective;
        private String orderNumber;

    }

    @Getter
    @Setter
    public static class SearchInvoice {
        private Integer partnershipIdx;
        private DateRange range;

        public enum DateRange {
            M1, M3, M6, Y1
        }
    }
}
