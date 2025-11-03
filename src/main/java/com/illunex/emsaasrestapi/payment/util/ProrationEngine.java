package com.illunex.emsaasrestapi.payment.util;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.DAYS;

@Component
public final class ProrationEngine {

    public ProrationResult calculate(ProrationInput in) {
        final RoundingMode RM = in.getRoundingMode();
        final String CURRENCY = in.getCurrency();

        var items = new ArrayList<ProrationResult.Item>();
        BigDecimal subtotal = BigDecimal.ZERO;

        // ───────────────────────────────────────────────────────────────
        // 0) 공통 스냅샷/분모/좌석 해석
        // ───────────────────────────────────────────────────────────────
        LocalDate periodStartOld = in.getPeriodStart();
        LocalDate periodEndOldExcl = in.getPeriodEndExcl();
        LocalDate today = in.getToday();

        // CHANGE POINT #1: 분모 정책
        // - 고정 31일을 쓰려면 in.getDenominatorDays()를 항상 31로 주입
        // - 달력 일수 사용 시: DAYS.between(periodStart, periodEndExcl)
        int D_OLD = (in.getDenominatorDaysOld() != null) ? in.getDenominatorDaysOld() : in.getDenominatorDays();
        int D_NEW = (in.getDenominatorDaysNew() != null)
                ? in.getDenominatorDaysNew()
                : ((in.getNextPeriodStart() != null && in.getNextPeriodEndExcl() != null)
                ? (int) DAYS.between(in.getNextPeriodStart(), in.getNextPeriodEndExcl())
                : in.getDenominatorDays());

        BigDecimal DbdOld = BigDecimal.valueOf(D_OLD);
        BigDecimal DbdNew = BigDecimal.valueOf(D_NEW);

        // fromPlan / toPlan 단가
        var fromPlan = in.getFromPlan();
        var toPlan   = in.getToPlan();
        BigDecimal fromUnit = (fromPlan != null && fromPlan.getPricePerUser() != null) ? fromPlan.getPricePerUser() : BigDecimal.ZERO;
        BigDecimal toUnit   = (toPlan   != null && toPlan.getPricePerUser()   != null) ? toPlan.getPricePerUser()   : BigDecimal.ZERO;

        // CHANGE POINT #2: 현 좌석 산정(신 플랜 잔여/RECURRING에 사용)
        // - 스냅샷 우선(useSnapshotSeatsFirst=true): LP.current_seat_count 중심
        // - 실시간(active): partnership 활성 멤버수 중심
        int baseMin = (fromPlan != null && fromPlan.getMinUserCount() != null) ? fromPlan.getMinUserCount() : 0;
        int active  = in.getCurrentActiveSeats();
        Integer snap = in.getSnapshotSeats();
        int seatSrc  = (in.isUseSnapshotSeatsFirst() ? (snap != null ? snap : active) : active);
        int chargeSeatsNow = Math.max(seatSrc, baseMin);

        // CHANGE POINT #3: 선불 좌석 정책(구 플랜 크레딧/미청구 기준)
        // - 기본: 직전 RECURRING quantity
        // - 폴백: minSeats 또는 activeSeats
        int prepaidSeats = Math.max(0, in.getPrepaidSeats());

        // ───────────────────────────────────────────────────────────────
        // 1) 미청구(구 플랜) — baseFrom ~ cutoff(보통 오늘 00:00, 즉 어제까지)
        // ───────────────────────────────────────────────────────────────
        // CHANGE POINT #4: 컷오프 경계
        // - TOMORROW 정책에서는 today(배타)까지 = 어제까지 정산
        // - NOW 정책도 일반적으로 today(배타)까지를 미청구 구간으로
        LocalDate baseFrom = (in.getBaseFrom() == null || in.getBaseFrom().isBefore(periodStartOld))
                ? periodStartOld
                : in.getBaseFrom();

        LocalDate cutoffExcl = (today.isBefore(baseFrom)) ? baseFrom : today; // [baseFrom, today)

        if (fromPlan != null && cutoffExcl.isAfter(baseFrom)) {
            // 이벤트를 cutoff 이전까지만 반영
            Map<LocalDate, Integer> deltaByDate = in.getSeatEvents().stream()
                    .filter(e -> !e.getDate().isBefore(baseFrom) && e.getDate().isBefore(cutoffExcl))
                    .collect(Collectors.groupingBy(ProrationInput.SeatEvent::getDate, TreeMap::new,
                            Collectors.summingInt(ProrationInput.SeatEvent::getDelta)));

            // CHANGE POINT #5: 이벤트 효력 시점
            // - 현재: "세그먼트 시작일 00:00"에 즉시 반영
            // - (변경 가능) "세그먼트 종료 시" 반영 등
            var bps = new TreeSet<LocalDate>();
            bps.add(baseFrom);
            bps.add(cutoffExcl);
            bps.addAll(deltaByDate.keySet());

            int runningSeats = prepaidSeats;
            LocalDate[] pts = bps.toArray(new LocalDate[0]);
            for (int i = 0; i < pts.length - 1; i++) {
                LocalDate segStart = pts[i];
                LocalDate segEndExcl = pts[i + 1];
                int days = (int) DAYS.between(segStart, segEndExcl);
                if (days <= 0) continue;

                Integer dlt = deltaByDate.get(segStart);
                if (dlt != null) runningSeats += dlt;

                int effectiveSeats = Math.max(runningSeats, baseMin);

                // CHANGE POINT #6: 미청구는 "선불 초과분(addAbovePrepaid)"만 과금
                int addAbovePrepaid = Math.max(0, effectiveSeats - prepaidSeats);
                if (addAbovePrepaid > 0) {
                    BigDecimal amt = fromUnit
                            .multiply(BigDecimal.valueOf(addAbovePrepaid))
                            .multiply(BigDecimal.valueOf(days))
                            .divide(DbdOld, 0, RM);

                    items.add(ProrationResult.Item.builder()
                            .itemType("PRORATION")
                            .description("좌석 변경 미청구분")
                            .quantity(addAbovePrepaid)
                            .unitPrice(fromUnit.setScale(0, RM).longValueExact())
                            .days(days)
                            .amount(amt.setScale(0, RM).longValueExact())
                            .meta(Map.of(
                                    "from", segStart.toString(),
                                    "to", segEndExcl.toString(),
                                    "numerator", days,
                                    "denominator", D_OLD,
                                    "planCd", fromPlan.getPlanCd()
                            ))
                            .build());
                    subtotal = subtotal.add(amt);
                }
            }
        }

        // ───────────────────────────────────────────────────────────────
        // 2) 잔여기간 NOW(업/다운/해지) — ActivationMode.NOW일 때만
        // ───────────────────────────────────────────────────────────────
        if (in.getActivationMode() == ProrationInput.ActivationMode.NOW) {
            int dRemain = Math.max(0, (int) DAYS.between(today, periodEndOldExcl));

            // CHANGE POINT #7: 잔여기간 과금/크레딧 정책
            // - 업그레이드 NOW: 신 플랜 잔여 × 현좌석 전액, + 구 플랜 잔여(선불 좌석) 크레딧
            // - 다운/해지 NOW: 필요 시 동일 패턴으로 분기
            if (in.getAction() == ProrationInput.Action.UPGRADE && toPlan != null && dRemain > 0) {
                // 신 플랜 잔여
                BigDecimal newRemain = toUnit
                        .multiply(BigDecimal.valueOf(chargeSeatsNow))
                        .multiply(BigDecimal.valueOf(dRemain))
                        .divide(DbdOld, 0, RM);

                items.add(ProrationResult.Item.builder()
                        .itemType("PRORATION")
                        .description("신 플랜 잔여기간 과금")
                        .quantity(chargeSeatsNow)
                        .unitPrice(toUnit.setScale(0, RM).longValueExact())
                        .days(dRemain)
                        .amount(newRemain.setScale(0, RM).longValueExact())
                        .meta(Map.of("numerator", dRemain, "denominator", D_OLD, "planCd", toPlan.getPlanCd()))
                        .build());
                subtotal = subtotal.add(newRemain);

                // 구 플랜 잔여 크레딧(선불 좌석만)
                if (prepaidSeats > 0) {
                    BigDecimal oldCredit = fromUnit
                            .multiply(BigDecimal.valueOf(prepaidSeats))
                            .multiply(BigDecimal.valueOf(dRemain))
                            .divide(DbdOld, 0, RM)
                            .negate();

                    items.add(ProrationResult.Item.builder()
                            .itemType("CREDIT")
                            .description("구 플랜 남은기간 크레딧")
                            .quantity(prepaidSeats)
                            .unitPrice(fromUnit.setScale(0, RM).longValueExact())
                            .days(dRemain)
                            .amount(oldCredit.setScale(0, RM).longValueExact())
                            .meta(Map.of("numerator", dRemain, "denominator", D_OLD, "planCd", fromPlan.getPlanCd()))
                            .build());
                    subtotal = subtotal.add(oldCredit);
                }
            }

            // (필요 시) DOWNGRADE NOW / CANCEL NOW도 동일 패턴으로 분기 추가
        }

        // ───────────────────────────────────────────────────────────────
        // 3) 내일부터 시작 RECURRING — ActivationMode.TOMORROW일 때만
        // ───────────────────────────────────────────────────────────────
        if (in.getActivationMode() == ProrationInput.ActivationMode.TOMORROW && toPlan != null) {
            LocalDate nextStart = in.getNextPeriodStart();
            LocalDate nextEndExcl = in.getNextPeriodEndExcl();

            // CHANGE POINT #8: RECURRING 계산 방식
            // - 기본: quantity × unit (days는 표시에만 사용)
            // - (변경 가능) 선불도 days 분할 일할로 표기/계산
            int minNew = (toPlan.getMinUserCount() != null) ? toPlan.getMinUserCount() : 0;
            int seatsForRecurring = Math.max(chargeSeatsNow, minNew);

            BigDecimal recurringAmt = toUnit.multiply(BigDecimal.valueOf(seatsForRecurring)).setScale(0, RM);

            items.add(ProrationResult.Item.builder()
                    .itemType("RECURRING")
                    .description("정기결제 선불")
                    .quantity(seatsForRecurring)
                    .unitPrice(toUnit.setScale(0, RM).longValueExact())
                    .days(D_NEW) // 표시용
                    .amount(recurringAmt.longValueExact())
                    .meta(Map.of(
                            "nextPeriodStart", nextStart.toString(),
                            "nextPeriodEndExcl", nextEndExcl.toString(),
                            "denominator", D_NEW,
                            "planCd", toPlan.getPlanCd()
                    ))
                    .build());
            subtotal = subtotal.add(recurringAmt);
        }

        // ───────────────────────────────────────────────────────────────
        // 4) 합계/세금/크레딧 이월
        // ───────────────────────────────────────────────────────────────
        // CHANGE POINT #9: 세금/부가세 정책
        long sub = subtotal.setScale(0, RM).longValueExact();
        long tax = 0L;               // 필요 시 세율 전략 주입
        long total = sub + tax;
        long carry = (total < 0) ? Math.abs(total) : 0L;

        // CHANGE POINT #10: 외부 표기용 분모 선택
        // - TOMORROW 모드면 NEW 분모, NOW면 OLD 분모
        int denomForDisplay = (in.getActivationMode() == ProrationInput.ActivationMode.TOMORROW) ? D_NEW : D_OLD;

        return ProrationResult.builder()
                .items(items)
                .subTotal(sub)
                .tax(tax)
                .total(total)
                .creditCarryOver(carry)
                .chargeSeatsResolved(chargeSeatsNow)
                .denominatorDays(denomForDisplay)
                .currency(CURRENCY)
                .roundingRule(RM.name())
                .periodStart((in.getActivationMode() == ProrationInput.ActivationMode.TOMORROW)
                        ? in.getNextPeriodStart() : periodStartOld)
                .periodEndExcl((in.getActivationMode() == ProrationInput.ActivationMode.TOMORROW)
                        ? in.getNextPeriodEndExcl() : periodEndOldExcl)
                .planName((toPlan != null ? toPlan.getName() : "N/A"))
                .build();
    }
}