package com.illunex.emsaasrestapi.payment;

import com.illunex.emsaasrestapi.common.code.EnumCode;
import com.illunex.emsaasrestapi.license.mapper.LicensePartnershipMapper;
import com.illunex.emsaasrestapi.license.vo.LicensePartnershipVO;
import com.illunex.emsaasrestapi.payment.mapper.InvoiceItemMapper;
import com.illunex.emsaasrestapi.payment.mapper.InvoiceMapper;
import com.illunex.emsaasrestapi.payment.mapper.SubscriptionChangeEventMapper;
import com.illunex.emsaasrestapi.payment.util.ProrationCalculator;
import com.illunex.emsaasrestapi.payment.vo.InvoiceItemVO;
import com.illunex.emsaasrestapi.payment.vo.InvoiceVO;
import com.illunex.emsaasrestapi.payment.vo.SubscriptionChangeEventVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentSchedule {
    private final LicensePartnershipMapper lpMapper;
    private final InvoiceMapper invoiceMapper;
    private final SubscriptionChangeEventMapper eventMapper;
    private final InvoiceItemMapper invoiceItemMapper; // 아래 XML 포함

    @Scheduled(cron = "0 0 2 * * ?", zone = "Asia/Seoul")
    public void generateMonthlyInvoiceBatch() {
        // 1) 후보 조회 (락 없이) - idx만 가볍게
        List<Integer> targetIds = lpMapper.selectIdxsByNextBillingDateToday();
        for (Integer lpId : targetIds) {
            try {
                generateMonthlyInvoiceTransactional(lpId);
            } catch (Exception e) {
                // 로그만 남기고 다음으로 (배치 계속 진행)
                log.error("Monthly invoice failed for lpId={}", lpId, e);
            }
        }
    }

    @Transactional
    void generateMonthlyInvoiceTransactional(Integer lpId) {
        // 2) 대상 LP를 잠금 조회 (동시성 제어)
        LicensePartnershipVO lp = lpMapper.selectByIdxForUpdate(lpId);
        if (lp == null) return;

        LocalDate today = LocalDate.now(); // Asia/Seoul
        if (!today.equals(lp.getNextBillingDate())) {
            // 이미 다른 인스턴스/트랜잭션이 처리했을 수 있음
            return;
        }

        // === 1) 인보이스 DRAFT 생성 (또는 멱등 보장) ===
        LocalDate periodStart = lp.getPeriodStartDate();
        LocalDate periodEnd   = lp.getPeriodEndDate(); // == next_billing_date
        int daysInMonth = YearMonth.from(periodStart).lengthOfMonth();

        InvoiceVO inv = new InvoiceVO();
        inv.setPartnershipIdx(lp.getPartnershipIdx());
        inv.setLicensePartnershipIdx(lp.getIdx());
        inv.setPeriodStart(periodStart);
        inv.setPeriodEnd(periodEnd);
        inv.setSubtotal(BigDecimal.ZERO);
        inv.setTax(BigDecimal.ZERO);
        inv.setTotal(BigDecimal.ZERO);
        inv.setStatusCd("ICS0001"); // DRAFT
        inv.setUnitCd("MUC0001");   // KRW 예시
        inv.setMeta(snapshot(lp, daysInMonth));

        try {
            invoiceMapper.insertByInvoiceVO(inv); // AUTO_INCREMENT idx 세팅
        } catch (org.springframework.dao.DuplicateKeyException dk) {
            // 동일기간 활성 인보이스가 이미 존재 (다른 트랜잭션이 선행)
            inv = invoiceMapper.selectActiveByPeriod(lp.getIdx(), periodStart, periodEnd);
            if (inv == null) {
                // 예외적인 경쟁상태면 재시도/중단
                throw dk;
            }
        }

        // === 2) PRORATION 항목 생성 ===
        List<SubscriptionChangeEventVO> events =
                eventMapper.selectByLpAndOccurredBetween(lp.getIdx(),
                        periodStart.atStartOfDay(), periodEnd.atStartOfDay());

        for (SubscriptionChangeEventVO ev : events) {
            InvoiceItemVO item = ProrationCalculator.buildProrationItem(lp, ev, periodStart, periodEnd);
            if (item != null) {
                item.setInvoiceIdx(inv.getIdx());
                invoiceItemMapper.insertByInvoiceItemVO(item);
            }
        }

        // === 3) RECURRING (다음 주기 선불) 생성 ===
        LocalDate nextPeriodStart = periodEnd;
        LocalDate nextPeriodEnd   = calcNextPeriodEnd(lp.getBillingDay(), nextPeriodStart);

        int chargeSeats = Math.max(lp.getCurrentSeatCount(), lp.getCurrentMinUserCount());
        BigDecimal amount = lp.getCurrentUnitPrice().multiply(new BigDecimal(chargeSeats));

        InvoiceItemVO recurring = new InvoiceItemVO();
        recurring.setInvoiceIdx(inv.getIdx());
        recurring.setItemTypeCd(EnumCode.InvoiceItem.ItemTypeCd.RECURRING.getCode()); // RECURRING
        recurring.setDescription("Recurring seats x unit price (prepaid)");
        recurring.setQuantity(chargeSeats);
        recurring.setUnitPrice(lp.getCurrentUnitPrice());
        recurring.setDays(null); // 정액
        recurring.setAmount(amount);
        recurring.setMeta(new JSONObject()
                .put("type", EnumCode.InvoiceItem.ItemTypeCd.RECURRING.getCode())
                .put("plan_unit_price", lp.getCurrentUnitPrice())
                .put("charge_seats", chargeSeats)
                .put("period_start", nextPeriodStart.toString())
                .put("period_end", nextPeriodEnd.toString())
                .toString());
        invoiceItemMapper.insertByInvoiceItemVO(recurring);

        // === 4) 합계 계산 ===
        invoiceMapper.recalcTotals(inv.getIdx(), new BigDecimal("0")); // VAT 10% 예시

        // === 5) 인보이스 OPEN 전환 ===
        invoiceMapper.markOpen(inv.getIdx());

        // === 6) LP 기간 롤오버 (반드시 잠금상태에서) ===
        lp.setPeriodStartDate(nextPeriodStart);
        lp.setPeriodEndDate(nextPeriodEnd);
        lp.setNextBillingDate(nextPeriodEnd);
        lpMapper.updatePeriod(lp);
    }

    private String snapshot(LicensePartnershipVO lp, int daysInMonth) {
        JSONObject meta = new JSONObject();
        meta.put("proration_basis", "CALENDAR_DAYS");
        meta.put("days_in_month", daysInMonth);
        meta.put("current_unit_price", lp.getCurrentUnitPrice());
        meta.put("current_min_user_count", lp.getCurrentMinUserCount());
        meta.put("plan_license_idx", lp.getLicenseIdx());
        return meta.toString();
    }

    /** billingDay(1~28) 앵커 규칙: 다음 주기의 종료일(= next billing date) 계산 */
    private LocalDate calcNextPeriodEnd(Integer billingDay, LocalDate nextPeriodStart) {
        // nextPeriodStart가 해당 주기의 시작(= 직전 period_end)
        YearMonth ym = YearMonth.from(nextPeriodStart);
        // 다음 anchor: 보통 "다음 달의 billingDay", 다만 2월 등 짧은 달은 말일로 clamp
        YearMonth ymNext = ym.plusMonths(1);
        int day = Math.min(billingDay, ymNext.lengthOfMonth());
        return LocalDate.of(ymNext.getYear(), ymNext.getMonth(), day);
    }
}
