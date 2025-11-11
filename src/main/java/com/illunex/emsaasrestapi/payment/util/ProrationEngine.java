package com.illunex.emsaasrestapi.payment.util;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
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
        LocalDate start = in.getPeriodStart(); //기존 구독 시작일
        LocalDate endExcl = in.getPeriodEndExcl(); //기존 구독 종료일
        LocalDate today = in.getAnchorDate(); //계산당시날짜
        LocalDate capEnd = today; //업그레이드 NOW 기준
        int dRemain = Math.max(0, (int) DAYS.between(today, endExcl));  // 잔여일수

        // from/to 단가
        BigDecimal fromUnit = in.getFromPlan().getPricePerUser();   //기존구독단가
        BigDecimal toUnit = (in.getToPlan() != null) ? in.getToPlan().getPricePerUser() : BigDecimal.ZERO; //신규구독단가

        // 현 좌석(신 플랜 전액 산정용)
        int baseMin = in.getToPlan().getMinUserCount(); //신규구독최소좌석수
        int active = in.getCurrentSeat();   //현재활성좌석수
//        int snapshot = (in.getSnapshotSeats() == null ? active : in.getSnapshotSeats());
        int chargeSeatsNow = Math.max(active, baseMin); //업그레이드 NOW 전용

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

    /**
     * 계산의 기준 날짜(업그레이드일)를 반드시 입력으로 받음.
     * LocalDate.now() 사용 금지(재현성 깨짐).
     */
    public ProrationResult calculate2(ProrationInput in) {
        // ===== 0) 입력 검증 =====
        requireNonNull(in.getFromPlan(), "fromPlan");
        requireNonNull(in.getToPlan(), "toPlan");
        requireNonNull(in.getPeriodStart(), "periodStart");
        requireNonNull(in.getPeriodEndExcl(), "periodEndExcl");
        requireNonNull(in.getPaidDate(), "paidDate");

        // 앵커일(업그레이드 기준일). 없으면 결제시간 날짜, 그것도 없으면 예외
        LocalDate anchorDate = in.getAnchorDate();

        // 기간 무결성: start ≤ paidDate ≤ anchorDate < periodEndExcl
        if ( !in.getPeriodStart().isBefore(in.getPeriodEndExcl()) )
            throw new IllegalArgumentException("invalid period range");
        if ( in.getPaidDate().isAfter(anchorDate) )
            throw new IllegalArgumentException("paidDate must be ≤ anchorDate");
        if ( !anchorDate.isBefore(in.getPeriodEndExcl()) )
            throw new IllegalArgumentException("anchorDate must be < periodEndExcl");

        // ===== 1) 파생 변수 =====
        final int D = days(in.getPeriodStart(), in.getPeriodEndExcl());          // 분모(해당 월 달력 일수)
        final int remainDays = Math.max(0, days(anchorDate, in.getPeriodEndExcl())); // 업그레이드 이후 남은 일수
        final BigDecimal fromUnit = in.getFromPlan().getPricePerUser();          // 구 플랜 단가
        final BigDecimal toUnit   = in.getToPlan().getPricePerUser();            // 신 플랜 단가
        final int oldMin = in.getFromPlan().getMinUserCount();                   // 구 플랜 최소 좌석
        final int newMin = in.getToPlan().getMinUserCount();                     // 신 플랜 최소 좌석
        final int snapshot = in.getSnapshotSeats();                              // 직전 인보이스(선불) 좌석 스냅샷
        final int activeAtUpgrade = in.getActiveSeats();                         // 업그레이드 시점 실제 좌석
        final int baseEff = Math.max(snapshot, oldMin);                          // 구 플랜 하한 반영한 "베이스라인"
        final int newEff  = Math.max(activeAtUpgrade, newMin);                   // 신 플랜 하한 반영한 "과금 좌석"
        final RoundingMode RM = in.getRoundingMode();

        List<ProrationResult.Item> items = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        // ===== 2) 구간 A: [paidDate, anchorDate) =====
        // "업그레이드 전" 좌석 변화 정산:
        // - 좌석이 베이스라인보다 많으면 PRORATION(+)
        // - 좌석이 베이스라인보다 적으면 CREDIT(-)
        // - 단, oldMin 하한 적용(하한 미만 감소는 CREDIT 아님)
        List<ProrationInput.SeatEvent> eventsA = normalizeAndSort(
                filterEvents(in.getSeatEvents(), in.getPaidDate(), anchorDate)
        );

        LocalDate cur = in.getPaidDate(); // 현재 세그먼트 시작점
        int cumDelta = 0;                 // paidDate 이후 누적 좌석 변화량(0에서 시작)

        for (ProrationInput.SeatEvent e : eventsA) {
            int d = days(cur, e.getOccurredAt()); // 세그먼트 일수
            if (d > 0) {
                // eff: 스냅샷 + 변화량, 단 oldMin 하한 보정
                int eff = Math.max(snapshot + cumDelta, oldMin);
                int diff = eff - baseEff; // 베이스라인과의 편차(+면 추가 과금, -면 크레딧)
                if (diff != 0) {
                    BigDecimal amt = prorate(fromUnit, diff, d, D, RM); // 항목 금액 일할 계산
                    items.add(makeItem(
                            diff > 0 ? "PRORATION" : "CREDIT",
                            "기존 라이센스 좌석 변화 정산",
                            Math.abs(diff),
                            roundUnitForDisplay(fromUnit, RM),
                            d,
                            toLongExact(amt),
                            metaA(cur, e.getOccurredAt(), D, baseEff, oldMin, in.getFromPlan().getPlanCd(), anchorDate)
                    ));
                    subtotal = subtotal.add(amt);
                }
            }
            // 이벤트 적용: 누적 변화량 갱신, 커서 이동
            cumDelta += e.getDelta();
            cur = e.getOccurredAt();
        }

        // 마지막 세그먼트(마지막 이벤트 ~ anchorDate)
        int tail = days(cur, anchorDate);
        if (tail > 0) {
            int eff = Math.max(snapshot + cumDelta, oldMin);
            int diff = eff - baseEff;
            if (diff != 0) {
                BigDecimal amt = prorate(fromUnit, diff, tail, D, RM);
                items.add(makeItem(
                        diff > 0 ? "PRORATION" : "CREDIT",
                        "기존 라이센스 좌석 변화 정산",
                        Math.abs(diff),
                        roundUnitForDisplay(fromUnit, RM),
                        tail,
                        toLongExact(amt),
                        metaA(cur, anchorDate, D, baseEff, oldMin, in.getFromPlan().getPlanCd(), anchorDate)
                ));
                subtotal = subtotal.add(amt);
            }
        }

        // ===== 3) 구간 B: [anchorDate, periodEndExcl) =====
        // 신 플랜 잔여기간(+) 과금 + 구 플랜 잔여기간(-) 크레딧을 동시에 반영
        if (remainDays > 0) {
            // 3-1) 신 플랜 잔여기간 과금: toUnit × newEff × remainDays / D
            BigDecimal newAmt = prorate(toUnit, newEff, remainDays, D, RM);
            items.add(makeItem(
                    "PRORATION",
                    "신규 라이센스 잔여기간 일할 계산 금액",
                    newEff,
                    roundUnitForDisplay(toUnit, RM),
                    remainDays,
                    toLongExact(newAmt),
                    metaB(remainDays, D, newMin, in.getToPlan().getPlanCd(), anchorDate)
            ));
            subtotal = subtotal.add(newAmt);

            // 3-2) 구 플랜 잔여기간 크레딧: - fromUnit × baseEff × remainDays / D
            BigDecimal oldCr = prorate(fromUnit, baseEff, remainDays, D, RM).negate();
            items.add(makeItem(
                    "CREDIT",
                    "구 플랜 잔여기간 크레딧",
                    baseEff,
                    roundUnitForDisplay(fromUnit, RM),
                    remainDays,
                    toLongExact(oldCr),
                    metaB(remainDays, D, oldMin, in.getFromPlan().getPlanCd(), anchorDate)
            ));
            subtotal = subtotal.add(oldCr);
        }

        // ===== 4) 합계/마감 =====
        long sub = toLongExact(subtotal.setScale(0, RM));
        long tax = 0L; // 필요 시 세금 규칙 연동
        long total = sub + tax;
        long carry = total < 0 ? Math.abs(total) : 0L; // 음수면 다음 결제에 이월

        return ProrationResult.builder()
                .items(items)
                .subTotal(sub)
                .tax(tax)
                .total(total)
                .creditCarryOver(carry)
                .chargeSeatsResolved(newEff)
                .denominatorDays(D)
                .currency(in.getCurrency())
                .roundingRule(RM.name())
                .periodStart(anchorDate)
                .periodEndExcl(in.getPeriodEndExcl())
                .build();
    }

    // =========================
    // ===== Helper Methods =====
    // =========================

    /** 두 날짜 차이(날짜2는 배타). 예: [10, 20) → 10일 */
    private int days(LocalDate from, LocalDate toExcl) {
        return (int) ChronoUnit.DAYS.between(from, toExcl);
    }

    /** 널 검사 헬퍼 */
    private void requireNonNull(Object o, String name) {
        if (o == null) throw new IllegalArgumentException(name + " is required");
    }

    /** 동일 날짜 이벤트 합산 + 날짜순 정렬 + 불변 리스트 반환 */
    private List<ProrationInput.SeatEvent> normalizeAndSort(List<ProrationInput.SeatEvent> events) {
        if (events == null || events.isEmpty()) return List.of();
        // TreeMap: 키(날짜) 기준 정렬, 같은 날 델타 합산
        Map<LocalDate, Integer> byDate = new TreeMap<>();
        for (ProrationInput.SeatEvent e : events) {
            byDate.merge(e.getOccurredAt(), e.getDelta(), Integer::sum);
        }
        List<ProrationInput.SeatEvent> out = new ArrayList<>(byDate.size());
        for (Map.Entry<LocalDate, Integer> en : byDate.entrySet()) {
            // relatedId 등 부가정보가 필요하면 확장
            out.add(new ProrationInput.SeatEvent(en.getKey(), en.getValue(), null));
        }
        return Collections.unmodifiableList(out);
    }

    /** 이벤트 필터: start ≤ occurredAt < endExcl 범위만 */
    private List<ProrationInput.SeatEvent> filterEvents(List<ProrationInput.SeatEvent> src,
                                                        LocalDate startIncl, LocalDate endExcl) {
        if (src == null || src.isEmpty()) return List.of();
        List<ProrationInput.SeatEvent> out = new ArrayList<>();
        for (ProrationInput.SeatEvent e : src) {
            LocalDate d = e.getOccurredAt();
            if (!d.isBefore(startIncl) && d.isBefore(endExcl)) {
                out.add(e);
            }
        }
        return out;
    }

    /** 일할 금액 계산: unit × seatsOrDiff × days / D  (항목별로 반올림) */
    private BigDecimal prorate(BigDecimal unit, int seatsOrDiff, int days, int D, RoundingMode rm) {
        // seatsOrDiff는 음수 가능(크레딧). days, D는 양수.
        BigDecimal amt = unit
                .multiply(BigDecimal.valueOf(seatsOrDiff))
                .multiply(BigDecimal.valueOf(days))
                .divide(BigDecimal.valueOf(D), 0, rm); // 원단위 반올림
        return amt;
    }

    /** 표기용 단가: 화면/메타에 보여줄 때만 반올림하여 사용(계산엔 원 단가 사용 권장) */
    private long roundUnitForDisplay(BigDecimal unit, RoundingMode rm) {
        return unit.setScale(0, rm).longValueExact();
    }

    /** BigDecimal → long 변환(초과/소수 에러 방지용) */
    private long toLongExact(BigDecimal bd) {
        return bd.longValueExact();
    }

    /** invoice_item 한 줄 만들기 */
    private ProrationResult.Item makeItem(String itemType, String description,
                                          int quantity, long unitPrice, int days, long amount,
                                          Map<String, Object> meta) {
        return ProrationResult.Item.builder()
                .itemType(itemType)          // "PRORATION" or "CREDIT"
                .description(description)    // 항목 설명
                .quantity(quantity)          // 좌석 수(또는 편차 절대값)
                .unitPrice(unitPrice)        // 표기용 단가(반올림 표시)
                .days(days)                  // 분자(해당 세그먼트 일수)
                .amount(amount)              // 최종 금액(±)
                .meta(meta)                  // 재현성 정보
                .build();
    }

    /** 메타: 업그레이드 전 구간 세그먼트 기록 */
    private Map<String, Object> metaA(LocalDate from, LocalDate toExcl, int denominator,
                                      int baseEff, int minUserCount, String planCd,
                                      LocalDate anchorDate) {
        return Map.of(
                "numerator", days(from, toExcl),     // 세그먼트 일수
                "denominator", denominator,          // 월 분모
                "baseline", baseEff,                 // 베이스 좌석(하한 반영)
                "min_user_count", minUserCount,      // oldMin
                "from", from.toString(),             // 세그먼트 시작
                "to", toExcl.toString(),             // 세그먼트 끝(배타)
                "planCd", planCd,                    // 구 플랜 코드
                "anchorDate", anchorDate.toString()  // 업그레이드 기준일
        );
    }

    /** 메타: 업그레이드 후 구간(잔여기간) 기록 */
    private Map<String, Object> metaB(int numerator, int denominator,
                                      int minUserCount, String planCd,
                                      LocalDate anchorDate) {
        return Map.of(
                "numerator", numerator,              // 잔여 일수
                "denominator", denominator,          // 월 분모
                "min_user_count", minUserCount,      // newMin or oldMin
                "planCd", planCd,                    // 플랜 코드
                "anchorDate", anchorDate.toString()  // 업그레이드 기준일
        );
    }
}