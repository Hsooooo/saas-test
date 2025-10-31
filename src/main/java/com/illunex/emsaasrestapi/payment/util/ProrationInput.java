package com.illunex.emsaasrestapi.payment.util;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.Singular;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Setter
@Getter
@Builder
public class ProrationInput {
    private Integer partnershipIdx; // 파트너쉽 번호
    private Integer licensePartnershipIdx; // 파트너쉽 구독 번호
    private Integer licenseIdx; // 라이센스 번호
    // 기존 구독 기간
    private LocalDate periodStart;   // inclusive
    private LocalDate periodEndExcl; // exclusive
    private LocalDate today;         // NOW 업그레이드 기준일(= occurredAt.toLocalDate)

    // 분모/반올림/통화
    private int denominatorDays;     // 분모 31 등
    private RoundingMode roundingMode; // HALF_UP
    private String currency;         // KRW

    // 계획(플랜)
    private Plan fromPlan;  // 현재 플랜 스냅샷
    private Plan toPlan;    // 목표 플랜 스냅샷 (UPGRADE/DOWNGRADE 시)
    @Getter @Builder
    public static class Plan {
        private int idx;
        private String planCd;
        private String name;
        private BigDecimal pricePerUser;
        private Integer minUserCount;
    }

    // 좌석
    private int prepaidSeats;       // 직전 RECURRING quantity (선불 좌석 수)
    private int minChargeSeats;     // 현재 구독의 min_user_count 스냅샷
    private int currentActiveSeats; // 현재 활성 멤버 수 스냅샷
    private boolean useSnapshotSeatsFirst; // true면 LP의 current_seat_count 우선
    private Integer snapshotSeats;  // LP.current_seat_count
    // NEW: 두 개의 분모를 구분(선택)
    private Integer denominatorDaysOld; // 미청구(구플랜) 계산용 분모
    private Integer denominatorDaysNew; // 신규 주기 RECURRING 표시용(선택)

    // NEW: 신규 주기 기간 (내일~)
    private LocalDate nextPeriodStart;   // tomorrow (inclusive)
    private LocalDate nextPeriodEndExcl; // nextPeriodStart + 1개월(또는 anchor 계산)

    // 액션
    private Action action;          // UPGRADE | DOWNGRADE | CANCEL
    private Effective effective;    // NOW | PERIOD_END
    private ActivationMode activationMode; // NOW | TOMORROW
    public enum Action { UPGRADE, DOWNGRADE, CANCEL }
    public enum Effective { NOW, PERIOD_END }
    public enum ActivationMode { NOW, TOMORROW }

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