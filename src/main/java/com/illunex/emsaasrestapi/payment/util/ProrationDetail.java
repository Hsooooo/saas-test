package com.illunex.emsaasrestapi.payment.util;


import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProrationDetail {
    public enum ItemType { RECURRING, PRORATION, CREDIT, ADJUSTMENT }

    private ItemType itemType;
    private String description;
    private int quantity;            // 좌석 수
    private long unitPrice;          // 단가(원, 소수없음 가정)
    private Integer days;            // 일할 일수(분자)
    private long amount;             // 계산 금액(마이너스 허용)
    private Integer relatedEventIdx; // subscription_change_event 참조
    private String metaJson;         // 분모/분자/단가/좌석증감/플랜버전 등
}