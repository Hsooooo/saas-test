package com.illunex.emsaasrestapi.payment.util;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.DAYS;

@Component
public final class ProrationEngine {

    public ProrationResult calculate(ProrationInput in) {
        if (in.getToPlan() == null) {
            throw new IllegalArgumentException("fromPlan is required");
        }
        final int D = (int) DAYS.between(in.getPeriodStart(), in.getPeriodEndExcl());
        final BigDecimal Dbd = BigDecimal.valueOf(D);
        final RoundingMode RM = in.getRoundingMode();

        // 기간 & 좌석 해석
        LocalDate start = in.getPeriodStart();
        LocalDate endExcl = in.getPeriodEndExcl();
        LocalDate today = in.getPaymentTime().toLocalDate();
        LocalDate capEnd = today;
        int dRemain = Math.max(0, (int) DAYS.between(today, endExcl));

        // from/to 단가
        BigDecimal fromUnit = in.getFromPlan().getPricePerUser();
        BigDecimal toUnit = (in.getToPlan() != null) ? in.getToPlan().getPricePerUser() : BigDecimal.ZERO;

        // 현 좌석(신 플랜 전액 산정용)
        int baseMin = in.getToPlan().getMinUserCount();
        int active = in.getCurrentSeat();
//        int snapshot = (in.getSnapshotSeats() == null ? active : in.getSnapshotSeats());
        int chargeSeatsNow = Math.max(active, baseMin);

        // 선불 좌석(구 플랜 크레딧용)
        int prepaidSeats = Math.max(in.getPaidSeat(), 0);

        // 미청구 시작점
        LocalDate baseFrom = in.getPaidDate();
        if (baseFrom.isBefore(start)) baseFrom = start;
        if (!capEnd.isAfter(baseFrom)) baseFrom = capEnd;

        // 이벤트 집계: 날짜 → delta 합산
        LocalDate finalBaseFrom = baseFrom;
        Map<LocalDate, Integer> deltaByDate = in.getSeatEvents().stream()
                .filter(e -> !e.getOccurredAt().isBefore(finalBaseFrom) && e.getOccurredAt().isBefore(capEnd))
                .collect(Collectors.groupingBy(ProrationInput.SeatEvent::getOccurredAt, TreeMap::new,
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
            LocalDate segEndExcl = pts[i + 1];
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
        if (in.getCaseType() == ProrationInput.CaseType.UPGRADE) {
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

    public ProrationResult calculate2(ProrationInput in) {
        // 0) 유효성
        if (in.getFromPlan() == null) {
            throw new IllegalArgumentException("fromPlan is required");
        }
        final ProrationInput.CaseType caseType = in.getCaseType();
        final boolean isUpgradeNow = caseType == ProrationInput.CaseType.UPGRADE;
        final boolean isFinalize = (caseType == ProrationInput.CaseType.DOWNGRADE
                || caseType == ProrationInput.CaseType.CANCEL);

        if (isUpgradeNow && in.getToPlan() == null) {
            throw new IllegalArgumentException("toPlan is required for upgrade");
        }

        // 1) 기간/분모/반올림
        final int D = (int) DAYS.between(in.getPeriodStart(), in.getPeriodEndExcl());
        final BigDecimal Dbd = BigDecimal.valueOf(D);
        final RoundingMode RM = in.getRoundingMode();

        LocalDate start   = in.getPeriodStart();
        LocalDate endExcl = in.getPeriodEndExcl();
        LocalDate today   = in.getPaymentTime().toLocalDate();

        // FINALIZE_*: 미청구를 주기 종료까지 끌고 간다. UPGRADE_NOW: 오늘까지만.
        LocalDate capEnd = isFinalize ? endExcl : today;

        // 남은 일수(업그레이드 NOW에서만 의미 있음)
        int dRemain = isUpgradeNow ? Math.max(0, (int) DAYS.between(today, endExcl)) : 0;

        // 2) 단가/최소과금 좌석
        BigDecimal fromUnit = in.getFromPlan().getPricePerUser();
        BigDecimal toUnit   = isUpgradeNow ? in.getToPlan().getPricePerUser() : BigDecimal.ZERO;

        int minFrom = in.getFromPlan().getMinUserCount();                    // ← 미청구(업그레이드 이전) 구간에 사용
        int minTo   = isUpgradeNow ? in.getToPlan().getMinUserCount() : 0;   // ← 업그레이드 잔여 구간에 사용

        int activeSeats       = in.getCurrentSeat();
        int chargeSeatsNow    = isUpgradeNow ? Math.max(activeSeats, minTo) : 0; // 업그레이드 NOW 전용
        int prepaidSeats      = Math.max(in.getPaidSeat(), 0);

        // 3) 미청구 시작점(직전 청구 시각, 주기 시작보다 과거면 start로 끌어올림)
        LocalDate baseFrom = in.getPaidDate();
        if (baseFrom.isBefore(start)) baseFrom = start;
        if (!capEnd.isAfter(baseFrom)) baseFrom = capEnd; // capEnd <= baseFrom 이면 미청구 없음

        // 4) 좌석 이벤트 집계 (baseFrom ≤ occurredAt < capEnd)
        LocalDate finalBaseFrom = baseFrom;
        Map<LocalDate, Integer> deltaByDate = in.getSeatEvents().stream()
                .filter(e -> !e.getOccurredAt().isBefore(finalBaseFrom) && e.getOccurredAt().isBefore(capEnd))
                .collect(Collectors.groupingBy(ProrationInput.SeatEvent::getOccurredAt, TreeMap::new,
                        Collectors.summingInt(ProrationInput.SeatEvent::getDelta)));

        // 5) 브레이크포인트 구성
        TreeSet<LocalDate> bps = new TreeSet<>();
        bps.add(baseFrom);
        bps.add(capEnd);
        bps.addAll(deltaByDate.keySet());

        var items = new ArrayList<ProrationResult.Item>();
        BigDecimal subTotal = BigDecimal.ZERO;

        // 러닝 좌석: 선불 좌석에서 시작
        int runningSeats = prepaidSeats;

        // 6) 미청구 스윕(업그레이드 이전 구간은 fromPlan 기준, FINALIZE_*도 동일)
        LocalDate[] pts = bps.toArray(new LocalDate[0]);
        for (int i = 0; i < pts.length - 1; i++) {
            LocalDate segStart   = pts[i];
            LocalDate segEndExcl = pts[i + 1];
            int days = (int) DAYS.between(segStart, segEndExcl);
            if (days <= 0) continue;

            Integer dlt = deltaByDate.get(segStart);
            if (dlt != null) runningSeats += dlt;

            int effectiveSeats = Math.max(runningSeats, minFrom); // ← fromPlan.min 으로 수정

            // 업그레이드 NOW에서는 업그레이드 시점(today) 이전 구간만 미청구 과금
            if (isUpgradeNow && segEndExcl.isAfter(today)) continue;

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
                        .meta(Map.of(
                                "numerator", days,
                                "denominator", D,
                                "from", segStart.toString(),
                                "to", segEndExcl.toString(),
                                "planCd", in.getFromPlan().getPlanCd()
                        ))
                        .build());
                subTotal = subTotal.add(amt);
            }
        }

        // 7) 케이스별 추가 항목
        if (isUpgradeNow) {
            // (A) 신 플랜 잔여 전액
            if (dRemain > 0 && chargeSeatsNow > 0) {
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
                        .meta(Map.of(
                                "numerator", dRemain,
                                "denominator", D,
                                "planCd", in.getToPlan().getPlanCd()
                        ))
                        .build());
                subTotal = subTotal.add(newRemain);
            }
            // (B) 구 플랜 잔여 크레딧(선불 좌석분)
            if (prepaidSeats > 0 && dRemain > 0) {
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
                        .meta(Map.of(
                                "numerator", dRemain,
                                "denominator", D,
                                "planCd", in.getFromPlan().getPlanCd()
                        ))
                        .build());
                subTotal = subTotal.add(oldCredit);
            }
        } else if (isFinalize) {
            // FINALIZE_* (다운그레이드/해지의 다음 정기 결제일): 미청구만 청구.
            // 여기서는 추가 항목 없음(선불/크레딧/선불정기 생략)
            // ※ 배치단계에서 plan 적용/RECURRENCE 생략/적용 여부를 분기
        }

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
                .chargeSeatsResolved(isUpgradeNow ? chargeSeatsNow : 0)
                .denominatorDays(D)
                .currency(in.getCurrency())
                .roundingRule(RM.name())
                .periodStart(start)
                .periodEndExcl(endExcl)
                .build();
    }
}