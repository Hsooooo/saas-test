package com.illunex.emsaasrestapi.payment.util;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class ProrationInput {
    // 기간
    private LocalDate periodStart;   // inclusive
    private LocalDate periodEndExcl; // exclusive
    private LocalDate today;         // NOW 업그레이드 기준일(= occurredAt.toLocalDate)

    // 분모/반올림/통화
    private int denominatorDays;     // 31 등
    private RoundingMode roundingMode; // HALF_UP
    private String currency;         // KRW

    // 계획(플랜)
    private Plan fromPlan;  // 현재 플랜 스냅샷
    private Plan toPlan;    // 목표 플랜 스냅샷 (UPGRADE/DOWNGRADE 시)
    @Getter @Builder
    public static class Plan {
        private int idx;
        private String planCd;
        private BigDecimal pricePerUser;
        private Integer minUserCount;
    }

    // 좌석
    private int prepaidSeats;       // 직전 RECURRING quantity (선불 좌석 수)
    private int minChargeSeats;     // 현재 구독의 min_user_count 스냅샷
    private int currentActiveSeats; // 현재 활성 멤버 수 스냅샷
    private boolean useSnapshotSeatsFirst; // true면 LP의 current_seat_count 우선
    private Integer snapshotSeats;  // LP.current_seat_count

    // 액션
    private Action action;          // UPGRADE | DOWNGRADE | CANCEL
    private Effective effective;    // NOW | PERIOD_END
    public enum Action { UPGRADE, DOWNGRADE, CANCEL }
    public enum Effective { NOW, PERIOD_END }

    // 이벤트(좌석)
    @Singular
    private List<SeatEvent> seatEvents; // baseFrom ~ capEnd 사이 ADD/REMOVE
    @Getter @Builder
    public static class SeatEvent {
        private LocalDate date;   // 이벤트 적용일(00:00)
        private int delta;        // +2, -1 등
        private Long relatedId;   // optional
    }

    // 계산 범위 제어
    private LocalDate baseFrom; // 미청구 정산 시작(직전 인보이스 issue_date vs periodStart)
    private LocalDate capEnd;   // 업그레이드 NOW면 today, 배치면 periodEndExcl
}