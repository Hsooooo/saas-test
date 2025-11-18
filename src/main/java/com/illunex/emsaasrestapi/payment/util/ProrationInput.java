package com.illunex.emsaasrestapi.payment.util;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;


@Getter
@Builder
public class ProrationInput {
    private final int paidSeat;               // 선불 결제된 좌석 수
    private final int currentSeat;            // 현재 좌석 수 (파트너쉽 멤버 상태 != DELETE 수)
    private final LocalDate paidDate;

    // 현재 주기
    private final LocalDate periodStart;     // inclusive
    private final LocalDate periodEndExcl;   // exclusive

    // 계산 기준 시각
    private final LocalDate anchorDate;      // 구독 변경 효력 발생일(케이스 1/2/3에 따라 필요)
    private final ZonedDateTime paymentTime; // T
    private final ZoneId zone;               // 테넌트 존

    // 요금(원/좌석)
    private final Plan fromPlan;             // 기구독 플랜(없으면 null)
    private final Plan toPlan;               // 타깃 플랜(케이스 1/2/3에 따라 필요)

    public ProrationInput.ProrationInputBuilder toBuilder() {
        return ProrationInput.builder()
                .paidSeat(this.paidSeat)
                .currentSeat(this.currentSeat)
                .paidDate(this.paidDate)
                .periodStart(this.periodStart)
                .periodEndExcl(this.periodEndExcl)
                .anchorDate(this.anchorDate)
                .paymentTime(this.paymentTime)
                .zone(this.zone)
                .fromPlan(this.fromPlan)
                .toPlan(this.toPlan);
    }

    @Getter @Builder
    public static class Plan {
        private final int idx;
        private final String planCd;
        private final String name;
        private final BigDecimal pricePerUser;
        private final Integer minUserCount;
    }

    // 좌석/이벤트
    private final Integer snapshotSeats;     // LP.current_seat_count (nullable)
    private final int activeSeats;           // 현재 활성 멤버 수
    private final boolean useSnapshotFirst;  // 프리뷰 재현성 위해 true 권장
    private final int startingSeatsForAccrual; // 미청구 구간 시작 시 좌석(기본: 직전 RECURRING 수량; 없으면 min(from))
    private final List<SeatEvent> seatEvents;  // 좌석 이벤트(기-내일자정 사이 전체)

    @Getter @Builder
    public static class SeatEvent {
        private final LocalDate occurredAt; // ZonedDateTime 원본
        private final int delta;                // +2, -1
        private final Long relatedId;

        public SeatEventBuilder toBuilder() {
            return SeatEvent.builder()
                    .occurredAt(this.occurredAt)
                    .delta(this.delta)
                    .relatedId(this.relatedId);
        }
    }

    // 통화/반올림
    private final RoundingMode roundingMode; // HALF_UP
    private final String currency;           // KRW

    // 케이스
    public enum CaseType { NEW_TO_PAID, UPGRADE, DOWNGRADE, CANCEL }
    private final CaseType caseType;

}