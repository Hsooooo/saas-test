package com.illunex.emsaasrestapi.payment.dto;

import com.illunex.emsaasrestapi.payment.util.ProrationResult;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class PaymentPreviewResult {
    private Integer partnershipIdx;
    private Integer fromLicenseIdx;
    private Integer toLicenseIdx;
    // 결제 연동 시 필요정보
    private String orderId;
    private String customerKey;
    private String billingKey;
    private String orderName;
    private Integer amount;
    private String customerEmail;
    private String customerName;

    // 라이센스 변경 시 결제금액 계산정보
    private List<PreviewResultItem> items;
    private LocalDate periodStart;
    private LocalDate periodEnd;


    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder(toBuilder = true)
    public static class PreviewResultItem {
        private String itemType;   // PRORATION | CREDIT | RECURRING | ADJUSTMENT
        private String description;
        private Integer quantity;      // 좌석 수
        private Long unitPrice;    // KRW 단위
        private Integer days;
        private Long amount;       // KRW 단위
        private Long relatedEventId;
        private Map<String, Object> meta;
    }

    /**
     * ProrationResult → PaymentPreviewResult 변환
     * - items 합계로 amount 산출
     * - 리스트는 불변으로 노출
     */
    public static PaymentPreviewResult of(ProrationResult prorationResult) {
        if (prorationResult == null) {
            throw new IllegalArgumentException("prorationResult must not be null");
        }

        final List<PreviewResultItem> mapped = prorationResult.getItems().stream()
                .map(item -> PreviewResultItem.builder()
                        .itemType(item.getItemType())           // "PRORATION" | "CREDIT" | "RECURRING" | "ADJUSTMENT"
                        .description(item.getDescription())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .days(item.getDays())
                        .amount(item.getAmount())
                        .relatedEventId(item.getRelatedEventId())
                        .meta(item.getMeta())
                        .build())
                .toList();

        final long total = mapped.stream()
                .map(PreviewResultItem::getAmount)
                .filter(a -> a != null && a != 0L)
                .mapToLong(Long::longValue)
                .sum();
        return PaymentPreviewResult.builder()
                .items(mapped)
                .periodStart(prorationResult.getPeriodStart())
                .periodEnd(prorationResult.getPeriodEndExcl())
                .amount(Math.toIntExact(total))
                .build();
    }

    @Override
    public String toString() {
        return "PaymentPreviewResult{" +
                "partnershipIdx=" + partnershipIdx +
                ", fromLicenseIdx=" + fromLicenseIdx +
                ", toLicenseIdx=" + toLicenseIdx +
                ", orderId='" + orderId + '\'' +
                ", customerKey='" + customerKey + '\'' +
                ", billingKey='" + billingKey + '\'' +
                ", orderName='" + orderName + '\'' +
                ", amount=" + amount +
                ", customerEmail='" + customerEmail + '\'' +
                ", customerName='" + customerName + '\'' +
                ", items=" + items +
                ", periodStart=" + periodStart +
                ", periodEnd=" + periodEnd +
                '}';
    }
}
