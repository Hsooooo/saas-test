package com.illunex.emsaasrestapi.payment.util;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class ProrationResult {
    @Singular
    private List<Item> items;
    private String planName;
    private long subTotal;
    private long tax;
    private long total;
    private long creditCarryOver;
    private int chargeSeatsResolved;    // 최종 사용한 "현 좌석" (신 플랜 계산 시)
    private int denominatorDays;
    private String currency;
    private String roundingRule;        // HALF_UP
    private LocalDate periodStart;
    private LocalDate periodEndExcl;

    @Getter @Builder
    public static class Item {
        private String itemType;   // PRORATION | CREDIT | RECURRING | ADJUSTMENT
        private String description;
        private int quantity;      // 좌석 수
        private long unitPrice;    // KRW 단위
        private int days;
        private long amount;       // KRW 단위
        private Long relatedEventId;
        private Map<String, Object> meta;

        @Override
        public String toString() {
            return "Item{" +
                    "itemType='" + itemType + '\'' +
                    ", description='" + description + '\'' +
                    ", quantity=" + quantity +
                    ", unitPrice=" + unitPrice +
                    ", days=" + days +
                    ", amount=" + amount +
                    ", relatedEventId=" + relatedEventId +
                    ", meta=" + meta +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "ProrationResult{" +
                "items=" + items +
                ", planName='" + planName + '\'' +
                ", subTotal=" + subTotal +
                ", tax=" + tax +
                ", total=" + total +
                ", creditCarryOver=" + creditCarryOver +
                ", chargeSeatsResolved=" + chargeSeatsResolved +
                ", denominatorDays=" + denominatorDays +
                ", currency='" + currency + '\'' +
                ", roundingRule='" + roundingRule + '\'' +
                ", periodStart=" + periodStart +
                ", periodEndExcl=" + periodEndExcl +
                '}';
    }
}