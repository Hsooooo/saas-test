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
        final int D = in.getDenominatorDays();
        final BigDecimal Dbd = BigDecimal.valueOf(D);
        final RoundingMode RM = in.getRoundingMode();

        // 기간 & 좌석 해석
        LocalDate start = in.getPeriodStart();
        LocalDate endExcl = in.getPeriodEndExcl();
        LocalDate today = in.getToday();
        LocalDate capEnd = in.getCapEnd();
        int dRemain = Math.max(0, (int) DAYS.between(today, endExcl));

        // from/to 단가
        BigDecimal fromUnit = in.getFromPlan().getPricePerUser();
        BigDecimal toUnit = (in.getToPlan() != null) ? in.getToPlan().getPricePerUser() : BigDecimal.ZERO;

        // 현 좌석(신 플랜 전액 산정용)
        int baseMin = in.getMinChargeSeats();
        int active = in.getCurrentActiveSeats();
        int snapshot = (in.getSnapshotSeats() == null ? active : in.getSnapshotSeats());
        int chargeSeatsNow = Math.max(in.isUseSnapshotSeatsFirst() ? snapshot : active, baseMin);

        // 선불 좌석(구 플랜 크레딧용)
        int prepaidSeats = Math.max(in.getPrepaidSeats(), 0);

        // 미청구 시작점
        LocalDate baseFrom = in.getBaseFrom();
        if (baseFrom.isBefore(start)) baseFrom = start;
        if (!capEnd.isAfter(baseFrom)) baseFrom = capEnd;

        // 이벤트 집계: 날짜 → delta 합산
        LocalDate finalBaseFrom = baseFrom;
        Map<LocalDate, Integer> deltaByDate = in.getSeatEvents().stream()
                .filter(e -> !e.getDate().isBefore(finalBaseFrom) && e.getDate().isBefore(capEnd))
                .collect(Collectors.groupingBy(ProrationInput.SeatEvent::getDate, TreeMap::new,
                        Collectors.summingInt(ProrationInput.SeatEvent::getDelta)));

        // 브레이크포인트
        TreeSet<LocalDate> bps = new TreeSet<>();
        bps.add(baseFrom);
        bps.add(capEnd);
        bps.addAll(deltaByDate.keySet());

        var items = new ArrayList<ProrationResult.Item>();
        BigDecimal subTotal = BigDecimal.ZERO;

        // 러닝 좌석 = 선불 좌석에서 시작
        int runningSeats = prepaidSeats;

        // 세그먼트 스윕: 업그레이드 이전 구간의 미청구(구 플랜)
        LocalDate[] pts = bps.toArray(new LocalDate[0]);
        for (int i = 0; i < pts.length - 1; i++) {
            LocalDate segStart = pts[i];
            LocalDate segEndExcl = pts[i+1];
            int days = (int) DAYS.between(segStart, segEndExcl);
            if (days <= 0) continue;

            Integer dlt = deltaByDate.get(segStart);
            if (dlt != null) runningSeats += dlt;

            int effectiveSeats = Math.max(runningSeats, baseMin);
            // 업그레이드 이전만 미청구 과금
            if (segEndExcl.isAfter(today)) continue;

            int addAbovePrepaid = Math.max(0, effectiveSeats - prepaidSeats);
            if (addAbovePrepaid > 0) {
                BigDecimal amt = fromUnit
                        .multiply(BigDecimal.valueOf(addAbovePrepaid))
                        .multiply(BigDecimal.valueOf(days))
                        .divide(Dbd, 0, RM);

                items.add(ProrationResult.Item.builder()
                        .itemType("PRORATION")
                        .description("좌석 변경 미청구분(구 플랜)")
                        .quantity(addAbovePrepaid)
                        .unitPrice(fromUnit.setScale(0, RM).longValueExact())
                        .days(days)
                        .amount(amt.setScale(0, RM).longValueExact())
                        .relatedEventId(null)
                        .meta(Map.of("numerator", days, "denominator", D,
                                "from", segStart.toString(), "to", segEndExcl.toString(),
                                "planCd", in.getFromPlan().getPlanCd()))
                        .build());
                subTotal = subTotal.add(amt);
            }
        }

        // 업그레이드/다운/해지 NOW 분기
        if (in.getAction() == ProrationInput.Action.UPGRADE && in.getEffective() == ProrationInput.Effective.NOW && dRemain > 0) {
            // 신 플랜 잔여 전액 (현 좌석)
            BigDecimal newRemain = toUnit
                    .multiply(BigDecimal.valueOf(chargeSeatsNow))
                    .multiply(BigDecimal.valueOf(dRemain))
                    .divide(Dbd, 0, RM);

            items.add(ProrationResult.Item.builder()
                    .itemType("PRORATION")
                    .description("신 플랜 잔여기간(업그레이드 NOW)")
                    .quantity(chargeSeatsNow)
                    .unitPrice(toUnit.setScale(0, RM).longValueExact())
                    .days(dRemain)
                    .amount(newRemain.setScale(0, RM).longValueExact())
                    .meta(Map.of("numerator", dRemain, "denominator", D, "planCd", in.getToPlan().getPlanCd()))
                    .build());
            subTotal = subTotal.add(newRemain);

            // 구 플랜 잔여 크레딧 (선불 좌석만)
            if (prepaidSeats > 0) {
                BigDecimal oldCredit = fromUnit
                        .multiply(BigDecimal.valueOf(prepaidSeats))
                        .multiply(BigDecimal.valueOf(dRemain))
                        .divide(Dbd, 0, RM)
                        .negate();

                items.add(ProrationResult.Item.builder()
                        .itemType("CREDIT")
                        .description("구 플랜 남은기간 크레딧(선불 좌석)")
                        .quantity(prepaidSeats)
                        .unitPrice(fromUnit.setScale(0, RM).longValueExact())
                        .days(dRemain)
                        .amount(oldCredit.setScale(0, RM).longValueExact())
                        .meta(Map.of("numerator", dRemain, "denominator", D, "planCd", in.getFromPlan().getPlanCd()))
                        .build());
                subTotal = subTotal.add(oldCredit);
            }
        }
        // (DOWNGRADE NOW, CANCEL NOW는 동일 패턴으로 추가 가능)

        long subtotal = subTotal.setScale(0, RM).longValueExact();
        long tax = 0L;
        long total = subtotal + tax;
        long carry = (total < 0) ? Math.abs(total) : 0L;

        return ProrationResult.builder()
                .items(items)
                .subTotal(subtotal)
                .tax(tax)
                .total(total)
                .creditCarryOver(carry)
                .chargeSeatsResolved(chargeSeatsNow)
                .denominatorDays(D)
                .currency(in.getCurrency())
                .roundingRule(RM.name())
                .periodStart(start)
                .periodEndExcl(endExcl)
                .build();
    }
}