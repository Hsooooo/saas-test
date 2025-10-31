package com.illunex.emsaasrestapi.payment.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentCommand {
    private int partnershipIdx;
    private int licenseIdx;          // 결제 대상 플랜(신규/업그레이드 포함)
    private Integer seatCount;       // 선택적. 없으면 lp.current_seat_count 사용
    private String reason;           // 메모/사유(업그레이드, 즉시결제 등)
}