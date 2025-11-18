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
import static java.util.stream.Collectors.toList;

@Component
public final class ProrationEngine {

    public ProrationResult calculateRecurringSeatProration(ProrationInput in, boolean allowDecreaseCredit) {
        // ===== 0) 입력 검증 =====
        requireNonNull(in.getFromPlan(), "fromPlan");
        requireNonNull(in.getPeriodStart(), "periodStart");
        requireNonNull(in.getPeriodEndExcl(), "periodEndExcl");
        requireNonNull(in.getPaidDate(), "paidDate");

        if (!in.getPeriodStart().isBefore(in.getPeriodEndExcl()))
            throw new IllegalArgumentException("invalid period range");
        if (in.getPaidDate().isBefore(in.getPeriodStart()) || !in.getPaidDate().isBefore(in.getPeriodEndExcl()))
            throw new IllegalArgumentException("paidDate must be within [periodStart, periodEndExcl)");

        // ===== 1) 파생 변수 =====
        final int D = days(in.getPeriodStart(), in.getPeriodEndExcl());          // 분모
        final BigDecimal unit = in.getFromPlan().getPricePerUser();              // 단가(구 플랜)
        final int oldMin = in.getFromPlan().getMinUserCount();                   // 구 플랜 최소 좌석
        final int snapshot = in.getSnapshotSeats();                              // 직전 인보이스 선불 좌석
        final int baseEff = Math.min(snapshot, oldMin);                           // 베이스라인
        final RoundingMode RM = in.getRoundingMode();

        final LocalDate evalStart = in.getPaidDate();
        final LocalDate evalEnd   = in.getPeriodEndExcl();                        // 정기정산은 주기 끝까지

        List<ProrationResult.Item> items = new ArrayList<>();
        long subtotal = 0L;

        // ===== 2) 이벤트 전처리: [paidDate, periodEndExcl) =====
        List<ProrationInput.SeatEvent> events = normalizeAndSort(
                filterEvents(in.getSeatEvents(), evalStart, evalEnd)
        );

        // ===== 3) 구간 스캔 =====
        LocalDate cur = evalStart;
        int cumDelta = 0; // paidDate 이후 누적 좌석 변화(스냅샷 기준)

        for (ProrationInput.SeatEvent e : events) {
            int days = days(cur, e.getOccurredAt());
            if (days > 0) {
                int eff  = Math.max(snapshot + cumDelta, oldMin); // 세그먼트 과금좌석
                int diff = eff - baseEff;                         // 베이스 대비 편차(+ 과금, − 크레딧)

                int billable = (diff < 0 && !allowDecreaseCredit) ? 0 : diff;
                if (billable != 0) {
                    long amt = toLongExact(prorate(unit, billable, days, D, RM)); // 아이템 내 원단위 반올림
                    items.add(makeItem(
                            "PRORATION",
                            billable > 0 ? "좌석 증가 일할 정산" : "좌석 감소 일할 크레딧",
                            Math.abs(billable),
                            roundUnitForDisplay(unit, RM),
                            days,
                            amt,
                            metaA(cur, e.getOccurredAt(), D, baseEff, oldMin, in.getFromPlan().getPlanCd(), /*anchor*/ evalStart)
                    ));
                    subtotal += amt;
                }
            }
            cumDelta += e.getDelta();
            cur = e.getOccurredAt();
        }

        // tail 세그먼트: 마지막 이벤트 ~ 주기 말
        int tail = days(cur, evalEnd);
        if (tail > 0) {
            int eff  = Math.max(snapshot + cumDelta, oldMin);
            int diff = eff - baseEff;

            int billable = (diff < 0 && !allowDecreaseCredit) ? 0 : diff;
            if (billable != 0) {
                long amt = toLongExact(prorate(unit, billable, tail, D, RM));
                items.add(makeItem(
                        "PRORATION",
                        billable > 0 ? "좌석 증가 일할 정산" : "좌석 감소 일할 크레딧",
                        Math.abs(billable),
                        roundUnitForDisplay(unit, RM),
                        tail,
                        amt,
                        metaA(cur, evalEnd, D, baseEff, oldMin, in.getFromPlan().getPlanCd(), /*anchor*/ evalStart)
                ));
                subtotal += amt;
            }
        }

        // ===== 4) 합계/반환 =====
        long tax = 0L;
        long total = subtotal + tax;
        long carry = total < 0 ? Math.abs(total) : 0L;

        return ProrationResult.builder()
                .items(items)
                .subTotal(subtotal)                  // 아이템 합 그대로
                .tax(tax)
                .total(total)
                .creditCarryOver(carry)
                .chargeSeatsResolved(0)              // 정기정산은 신플랜 좌석결정 없음
                .denominatorDays(D)
                .currency(in.getCurrency())
                .roundingRule(RM.name())
                .periodStart(in.getPeriodStart())    // 원래 주기
                .periodEndExcl(in.getPeriodEndExcl())
                .build();
    }

    /**
     * 계산의 기준 날짜(업그레이드일)를 반드시 입력으로 받음.
     * LocalDate.now() 사용 금지(재현성 깨짐).
     */
    public ProrationResult calculate2(ProrationInput in) {
        // ===== 0) 입력 검증 =====
        requireNonNull(in.getFromPlan(), "fromPlan");
//        requireNonNull(in.getToPlan(), "toPlan");
        requireNonNull(in.getPeriodStart(), "periodStart");
        requireNonNull(in.getPeriodEndExcl(), "periodEndExcl");
        requireNonNull(in.getPaidDate(), "paidDate");

        // 앵커일(업그레이드 기준일). 없으면 결제시간 날짜, 그것도 없으면 예외
        LocalDate anchorDate = in.getAnchorDate();
        LocalDate nextPeriodStart = anchorDate.plusDays(1);
        LocalDate nextPeriodEndExcl = nextPeriodStart.plusMonths(1);

        // 기간 무결성: start ≤ paidDate ≤ anchorDate < periodEndExcl
        if ( !in.getPeriodStart().isBefore(in.getPeriodEndExcl()) )
            throw new IllegalArgumentException("invalid period range");
        if ( in.getPaidDate().isAfter(anchorDate) )
            throw new IllegalArgumentException("paidDate must be ≤ anchorDate");
//        if ( !anchorDate.isBefore(in.getPeriodEndExcl()) )
//            throw new IllegalArgumentException("anchorDate must be < periodEndExcl");

        // ===== 1) 파생 변수 =====
        final int D = days(in.getPeriodStart(), in.getPeriodEndExcl());          // 분모(해당 월 달력 일수)
        final int remainDays = Math.max(0, days(anchorDate, in.getPeriodEndExcl())); // 업그레이드 이후 남은 일수
        final BigDecimal fromUnit = in.getFromPlan().getPricePerUser();          // 구 플랜 단가

        final int oldMin = in.getFromPlan().getMinUserCount();                   // 구 플랜 최소 좌석
        int newEff = 0;
        final int snapshot = in.getSnapshotSeats();                              // 직전 인보이스(선불) 좌석 스냅샷
        final int activeAtUpgrade = in.getActiveSeats();                         // 업그레이드 시점 실제 좌석
        final int baseEff = oldMin;                          // 구 플랜 하한 반영한 "베이스라인"

        final RoundingMode RM = in.getRoundingMode();

        List<ProrationResult.Item> items = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        // ===== 2) 구간 A: [paidDate, anchorDate) =====
        // "업그레이드 전" 좌석 변화 정산:
        // - 좌석이 베이스라인보다 많으면 PRORATION(+)
        // - 좌석이 베이스라인보다 적으면 CREDIT(-)
        // - 단, oldMin 하한 적용(하한 미만 감소는 CREDIT 아님)
        List<ProrationInput.SeatEvent> eventsA = normalizeAndSortForBilling(in.getSeatEvents(), in.getPaidDate(), anchorDate);
//        List<ProrationInput.SeatEvent> eventsA = normalizeAndSort(
//                filterEvents(in.getSeatEvents(), in.getPaidDate(), anchorDate)
//        );

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
                            "PRORATION",
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
                        "PRORATION",
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

        if (in.getCaseType().equals(ProrationInput.CaseType.UPGRADE)) {
            final int newMin = in.getToPlan().getMinUserCount();                     // 신 플랜 최소 좌석
            final BigDecimal toUnit   = in.getToPlan().getPricePerUser();            // 신 플랜 단가
            newEff  = Math.max(activeAtUpgrade, newMin);                   // 신 플랜 하한 반영한 "과금 좌석"

            // ===== 3) 구간 B: [anchorDate, periodEndExcl) =====
            // 신 플랜 잔여기간(+) 과금 + 구 플랜 잔여기간(-) 크레딧을 동시에 반영
            if (remainDays > 0) {
                // 3-2) 구 플랜 잔여기간 크레딧: - fromUnit × baseEff × remainDays / D
                BigDecimal oldCr = prorate(fromUnit, baseEff, remainDays, D, RM).negate();
                items.add(makeItem(
                        "PRORATION",
                        "구 플랜 잔여기간 크레딧",
                        baseEff,
                        roundUnitForDisplay(fromUnit, RM),
                        remainDays,
                        toLongExact(oldCr),
                        metaB(remainDays, D, oldMin, in.getFromPlan().getPlanCd(), anchorDate)
                ));
                subtotal = subtotal.add(oldCr);
            }
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
                .periodStart(in.getPeriodStart())
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
        LocalDate from = anchorDate;
        LocalDate toExcl = anchorDate.plusDays(numerator);

        return Map.of(
                "numerator", numerator,              // 잔여 일수
                "denominator", denominator,          // 월 분모
                "min_user_count", minUserCount,      // newMin or oldMin
                "planCd", planCd,                    // 플랜 코드
                "anchorDate", anchorDate.toString(), // 업그레이드 기준일
                "from", from.toString(),             // 잔여 구간 시작
                "to", toExcl.toString()              // 잔여 구간 끝(배타)
        );
    }

    private List<ProrationInput.SeatEvent> normalizeAndSortForBilling(
            List<ProrationInput.SeatEvent> raw,
            LocalDate paidDate,
            LocalDate anchorDate
    ) {
        return raw.stream()
                .map(e -> {
                    LocalDate effective = e.getOccurredAt().plusDays(1); // D+1 반영
                    return e.toBuilder()
                            .occurredAt(effective)
                            .build();
                })
                // 과금 기간 밖은 컷
                .filter(e -> !e.getOccurredAt().isBefore(paidDate)
                        && e.getOccurredAt().isBefore(anchorDate))
                .sorted(Comparator.comparing(ProrationInput.SeatEvent::getOccurredAt))
                .collect(toList());
    }
}