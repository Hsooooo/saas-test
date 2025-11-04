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

    // ─────────────────────────────────────────────────────────────
    private static BigDecimal flushSegment(
            List<ProrationResult.Item> items,
            ProrationInput.Plan fromPlan,
            BigDecimal unit,
            RoundingMode RM,
            BigDecimal DbdOld,
            int D_OLD,
            LocalDate segStart,
            LocalDate segEndIncl,
            int segAdd,
            int segDays,
            BigDecimal subtotal
    ) {
        if (segStart == null || segDays <= 0 || segAdd <= 0) return subtotal;

        BigDecimal amt = unit
                .multiply(BigDecimal.valueOf(segAdd))
                .multiply(BigDecimal.valueOf(segDays))
                .divide(DbdOld, 0, RM);

        if (amt.signum() != 0) {
            items.add(ProrationResult.Item.builder()
                    .itemType("PRORATION")
                    .description("미청구 사용분(구간)")
                    .quantity(segAdd)
                    .unitPrice(unit.setScale(0, RM).longValueExact())
                    .days(segDays)
                    .amount(amt.longValueExact())
                    .meta(Map.of(
                            "from", segStart.toString(),
                            "to", segEndIncl.toString(),
                            "numerator", segDays,
                            "denominator", D_OLD,
                            "planCd", fromPlan.getPlanCd()
                    ))
                    .build());
            return subtotal.add(amt);
        }
        return subtotal;
    }

    private static <T> T requireNonNull(T v, String name) {
        if (v == null) throw new IllegalArgumentException(name + " is null");
        return v;
    }
}
//    private static int nz(Integer v) { return v == null ? 0 : v; }
//    private static BigDecimal nz(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }

//    private static LocalDate resolveBaseFromDay(ProrationInput in, java.time.ZoneId Z) {
//        // 마지막 paidDate || 마지막 invoice.issueDate || periodStart
//        ZonedDateTime baseFromTs = null;
//        try {
//            // 선택 필드로 가정: 있으면 사용
//            baseFromTs = in.getBaseFrom();
//        } catch (Throwable ignore) { /* 필드 없을 수 있음 */ }
//
//        if (baseFromTs == null) {
//            // 서비스 레이어에서 이미 보정했다고 가정하고 periodStart 사용
//            return in.getPeriodStart();
//        }
//        return baseFromTs.withZoneSameInstant(Z).toLocalDate();
//    }

//    private static ProrationResult finalize(
//            ProrationInput in,
//            List<ProrationResult.Item> items,
//            BigDecimal subtotal,
//            int denomDisplay,
//            String planNameForDisplay
//    ) {
//        long sub = subtotal.setScale(0, in.getRoundingMode()).longValueExact();
//        long tax = 0L;
//        long total = sub + tax;
//        long carry = total < 0 ? Math.abs(total) : 0L;
//
//        return ProrationResult.builder()
//                .items(items)
//                .subTotal(sub)
//                .tax(tax)
//                .total(total)
//                .creditCarryOver(carry)
//                .denominatorDays(denomDisplay)
//                .currency(in.getCurrency())
//                .roundingRule(in.getRoundingMode().name())
//                .periodStart(in.getPeriodStart())
//                .periodEndExcl(in.getPeriodEndExcl())
//                .planName(planNameForDisplay)
//                .build();
//    }

//    public ProrationResult calculateHs(ProrationInput input) {
//        if (input.getToPlan() == null) {
//            throw new IllegalArgumentException("toPlan is required");
//        }
//        final RoundingMode RM = input.getRoundingMode();
//        final LocalDate tomorrow = input.getPaymentTime().toLocalDate().plusDays(1);
//        final ProrationInput.Plan toPlan = input.getToPlan();
//        final BigDecimal recurringAmt = nz(toPlan.getPricePerUser())
//                .multiply(BigDecimal.valueOf(input.getActiveSeats()))
//                .setScale(0, RM);
//        //#### 1. 신규구독/구독변경 체크 ####
//        List<ProrationResult.Item> itemList = new ArrayList<>();
//        // 기존 구독 있는 경우
//        if (input.getFromPlan() != null) {
//            // 기존 기결제된 인원수
//            int paidSeat = input.getPaidSeat();
//            BigDecimal fromUnit = input.getFromPlan().getPricePerUser();
////            LocalDate finalBaseFrom = baseFrom;
//            Map<LocalDate, Integer> deltaByDate = input.getSeatEvents().stream()
////                    .filter(e -> !e.getOccurredAt().isBefore(finalBaseFrom) && e.getOccurredAt().isBefore(capEnd))
//                    .collect(Collectors.groupingBy(ProrationInput.SeatEvent::getOccurredAt, TreeMap::new,
//                    Collectors.summingInt(ProrationInput.SeatEvent::getDelta)));
//            // 브레이크포인트
//            TreeSet<LocalDate> bps = new TreeSet<>();
//            bps.add(input.getPaidDate());
//            bps.add(input.getPaymentTime().toLocalDate());
//            bps.addAll(deltaByDate.keySet());
//
//            LocalDate[] pts = bps.toArray(new LocalDate[0]);
//            for (int i = 0; i < pts.length - 1; i++) {
//                LocalDate segStart = pts[i];
//                LocalDate segEndExcl = pts[i+1];
//                int days = (int) DAYS.between(segStart, segEndExcl);
//                if (days <= 0) continue;
//                Integer dlt = deltaByDate.get(segStart);
//                if (dlt != null) paidSeat += dlt;
//                int effectiveSeats = Math.max(paidSeat, baseMin);
//                // 업그레이드 이전만 미청구 과금
//                if (segEndExcl.isAfter(input.getPaidDate())) continue;
//                int addAbovePrepaid = Math.max(0, effectiveSeats - paidSeat);
//                if (addAbovePrepaid > 0) {BigDecimal amt = fromUnit
//                        .multiply(BigDecimal.valueOf(addAbovePrepaid))
//                        .multiply(BigDecimal.valueOf(days))
//                        .divide(Dbd, 0, RM);
//                    itemList.add(ProrationResult.Item.builder().itemType("PRORATION").description("좌석 변경 미청구분(구 플랜)")
//                     .quantity(addAbovePrepaid)
//                     .unitPrice(fromUnit.setScale(0, RM).longValueExact())
//                        .days(days)
//                         .amount(amt.setScale(0, RM).longValueExact())
//                        .relatedEventId(null)
//                        .meta(Map.of("numerator", days, "denominator", D,
//                                "from", segStart.toString(), "to", segEndExcl.toString(),
//                                 "planCd", in.getFromPlan().getPlanCd()))
//  .build());
//              subTotal = subTotal.add(amt);
//           }
//
//        }
//        // 정기결제액은 무조건
//        itemList.add(ProrationResult.Item.builder()
//                .itemType("RECURRING")
//                .description("정기결제 선불(내일 00:00 시작)")
//                .quantity(input.getActiveSeats())
//                .unitPrice(nz(toPlan.getPricePerUser()).setScale(0, RoundingMode.HALF_UP).longValueExact())
//                .days((int) DAYS.between(tomorrow, tomorrow.plusMonths(1))) // 표기용
//                .amount(recurringAmt.longValueExact())
//                .meta(Map.of("planCd", toPlan.getPlanCd()))
//                .build());
//        return null;
//    }
//}

/**
 *         LocalDate finalBaseFrom = baseFrom;
 *         Map<LocalDate, Integer> deltaByDate = in.getSeatEvents().stream()
 *                 .filter(e -> !e.getDate().isBefore(finalBaseFrom) && e.getDate().isBefore(capEnd))
 *                 .collect(Collectors.groupingBy(ProrationInput.SeatEvent::getDate, TreeMap::new,
 *                         Collectors.summingInt(ProrationInput.SeatEvent::getDelta)));
 *
 *         // 브레이크포인트
 *         TreeSet<LocalDate> bps = new TreeSet<>();
 *         bps.add(baseFrom);
 *         bps.add(capEnd);
 *         bps.addAll(deltaByDate.keySet());
 *
 *         var items = new ArrayList<ProrationResult.Item>();
 *         BigDecimal subTotal = BigDecimal.ZERO;
 *
 *         // 러닝 좌석 = 선불 좌석에서 시작
 *         int runningSeats = prepaidSeats;
 *
 *         // 세그먼트 스윕: 업그레이드 이전 구간의 미청구(구 플랜)
 *         LocalDate[] pts = bps.toArray(new LocalDate[0]);
 *         for (int i = 0; i < pts.length - 1; i++) {
 *             LocalDate segStart = pts[i];
 *             LocalDate segEndExcl = pts[i+1];
 *             int days = (int) DAYS.between(segStart, segEndExcl);
 *             if (days <= 0) continue;
 *
 *             Integer dlt = deltaByDate.get(segStart);
 *             if (dlt != null) runningSeats += dlt;
 *
 *             int effectiveSeats = Math.max(runningSeats, baseMin);
 *             // 업그레이드 이전만 미청구 과금
 *             if (segEndExcl.isAfter(today)) continue;
 *
 *             int addAbovePrepaid = Math.max(0, effectiveSeats - prepaidSeats);
 *             if (addAbovePrepaid > 0) {
 *                 BigDecimal amt = fromUnit
 *                         .multiply(BigDecimal.valueOf(addAbovePrepaid))
 *                         .multiply(BigDecimal.valueOf(days))
 *                         .divide(Dbd, 0, RM);
 *
 *                 items.add(ProrationResult.Item.builder()
 *                         .itemType("PRORATION")
 *                         .description("좌석 변경 미청구분(구 플랜)")
 *                         .quantity(addAbovePrepaid)
 *                         .unitPrice(fromUnit.setScale(0, RM).longValueExact())
 *                         .days(days)
 *                         .amount(amt.setScale(0, RM).longValueExact())
 *                         .relatedEventId(null)
 *                         .meta(Map.of("numerator", days, "denominator", D,
 *                                 "from", segStart.toString(), "to", segEndExcl.toString(),
 *                                 "planCd", in.getFromPlan().getPlanCd()))
 *                         .build());
 *                 subTotal = subTotal.add(amt);
 *             }
 *         }
 */