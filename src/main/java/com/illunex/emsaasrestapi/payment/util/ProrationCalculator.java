package com.illunex.emsaasrestapi.payment.util;
import com.illunex.emsaasrestapi.common.code.EnumCode;
import com.illunex.emsaasrestapi.license.vo.LicensePartnershipVO;
import com.illunex.emsaasrestapi.payment.vo.InvoiceItemVO;
import com.illunex.emsaasrestapi.payment.vo.SubscriptionChangeEventVO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;

public final class ProrationCalculator {
    private ProrationCalculator() {}

    public static InvoiceItemVO buildProrationItem(
            LicensePartnershipVO lp,
            SubscriptionChangeEventVO ev,
            LocalDate periodStart,
            LocalDate periodEnd
    ) {
        LocalDate effStart = ev.getOccurredDate().toLocalDate();
        if (!effStart.isBefore(periodEnd)) return null; // 주기 밖
        if (effStart.isBefore(periodStart)) effStart = periodStart;

        int usedDays = (int) ChronoUnit.DAYS.between(effStart, periodEnd);
        if (usedDays <= 0) return null;

        int daysInMonth = YearMonth.from(periodStart).lengthOfMonth();
        int chargeSeats = Math.max(lp.getCurrentSeatCount(), lp.getCurrentMinUserCount());

        switch (ev.getTypeCd()) {
            case "CET0001", "CET0002": // ADD_SEAT
            {
                int qtyDelta = ev.getQtyDelta(); // +/-
                if (qtyDelta == 0) return null;

                BigDecimal perUserDaily = lp.getCurrentUnitPrice()
                        .divide(new BigDecimal(daysInMonth), 8, java.math.RoundingMode.HALF_UP);
                BigDecimal amount = perUserDaily
                        .multiply(new BigDecimal(usedDays))
                        .multiply(new BigDecimal(qtyDelta))
                        .setScale(2, java.math.RoundingMode.HALF_UP);

                InvoiceItemVO item = new InvoiceItemVO();
                item.setItemTypeCd(qtyDelta > 0 ? "ITC0002" : "ITC0003"); // PRORATION / CREDIT
                item.setDescription(qtyDelta > 0 ? "Proration for added seats" : "Credit for removed seats");
                item.setQuantity(Math.abs(qtyDelta));
                item.setUnitPrice(lp.getCurrentUnitPrice());
                item.setDays(usedDays);
                item.setAmount(amount); // 음수 가능(CREDIT)
                item.setRelatedEventIdx(ev.getIdx());
                item.setMeta(new org.json.JSONObject()
                        .put("basis", "CALENDAR_DAYS")
                        .put("days_in_month", daysInMonth)
                        .put("used_days", usedDays)
                        .put("qty_delta", qtyDelta)
                        .toString());
                return item;
            }
            case "CET0003": // PLAN_UPGRADE
            case "CET0004": // PLAN_DOWNGRADE
            {
                // 업/다운그레이드는 일반적으로: 새 플랜 일할(+), 기존 플랜 잔여(-)를 분리 아이템으로 생성
                // 정기 배치에서는 '즉시 정산'이 이미 처리된 이벤트라면 스킵하는 정책 선택 가능.
                // 여기서는 정기결제 시점에 남은 미청구가 있다면 반영하도록 예시 로직 1건만 반환(간단화).
                // 실제 운영에서는 업/다운그레이드 즉시 invoice로 분리 정산 권장.
                return null;
            }
            default:
                return null;
        }
    }
}
