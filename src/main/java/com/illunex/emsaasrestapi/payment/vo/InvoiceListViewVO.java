package com.illunex.emsaasrestapi.payment.vo;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.time.LocalDate;
import java.time.ZonedDateTime;

@Getter
@Setter
@Alias("InvoiceListViewVO")
public class InvoiceListViewVO {
    private Integer invoiceIdx;
    private ZonedDateTime payDate;
    private String planName;
    private Integer seatCount;
    private Integer amount;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private String typeCd;
    private Integer invLicenseIdx;
    private Integer lpLicenseIdx;
    private String lpStateCd;
    private String displayState;
    private String receiptUrl;

    enum DisplayState { 구독중, 구독중_변경예약, 구독중_해지예약, 기한만료 }

    DisplayState resolveState(InvoiceListViewVO r, LocalDate today) {
        // 조정(IIT0002): 앵커 전까지만 구독중
        if ("IIT0002".equals(r.typeCd)) {
            boolean inRange = !today.isBefore(r.periodStart) && today.isBefore(r.periodEnd);
            return inRange ? DisplayState.구독중 : DisplayState.기한만료;
        }
        // 크레딧/환불(IIT0003): 항상 기한만료(목록 표기 정책 고정)
        if ("IIT0003".equals(r.typeCd)) return DisplayState.기한만료;

        // 정기(IIT0001)
        if (!today.isBefore(r.periodEnd) ) return DisplayState.기한만료;     // today >= period_end
        if (today.isBefore(r.periodStart)) return DisplayState.구독중;        // 구독예정도 '구독중' 표기 정책

        // 중도 업/다운 이후 과거 청구: 스냅샷 플랜 ≠ 현재 LP 플랜
        if (r.lpLicenseIdx != null && r.invLicenseIdx != null && !r.lpLicenseIdx.equals(r.invLicenseIdx))
            return DisplayState.기한만료;

        // 현재 구간 + LP 상태 라벨
        if ("LPS0003".equals(r.lpStateCd)) return DisplayState.구독중_변경예약;
        if ("LPS0004".equals(r.lpStateCd)) return DisplayState.구독중_해지예약;
        return DisplayState.구독중;
    }
}
