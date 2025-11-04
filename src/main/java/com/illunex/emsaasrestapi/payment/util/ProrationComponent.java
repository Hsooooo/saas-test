package com.illunex.emsaasrestapi.payment.util;

import com.illunex.emsaasrestapi.common.CustomException;
import com.illunex.emsaasrestapi.common.ErrorCode;
import com.illunex.emsaasrestapi.common.code.EnumCode;
import com.illunex.emsaasrestapi.license.mapper.LicenseMapper;
import com.illunex.emsaasrestapi.license.mapper.LicensePartnershipMapper;
import com.illunex.emsaasrestapi.license.mapper.LicensePaymentHistoryMapper;
import com.illunex.emsaasrestapi.license.vo.LicensePartnershipVO;
import com.illunex.emsaasrestapi.license.vo.LicenseVO;
import com.illunex.emsaasrestapi.member.vo.MemberVO;
import com.illunex.emsaasrestapi.partnership.mapper.PartnershipMemberMapper;
import com.illunex.emsaasrestapi.payment.dto.RequestPaymentDTO;
import com.illunex.emsaasrestapi.payment.mapper.InvoiceItemMapper;
import com.illunex.emsaasrestapi.payment.mapper.InvoiceMapper;
import com.illunex.emsaasrestapi.payment.mapper.SubscriptionChangeEventMapper;
import com.illunex.emsaasrestapi.payment.vo.InvoiceVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ProrationComponent {
    private final LicensePartnershipMapper licensePartnershipMapper;
    private final LicenseMapper licenseMapper;
    private final LicensePaymentHistoryMapper licensePaymentHistoryMapper;
    private final InvoiceMapper invoiceMapper;
    private final PartnershipMemberMapper partnershipMemberMapper;
    private final SubscriptionChangeEventMapper subscriptionChangeEventMapper;
    private final InvoiceItemMapper invoiceItemMapper;

    public ProrationInput buildInputForPreview(RequestPaymentDTO.SubscriptionInfo req, MemberVO member) throws CustomException {
        final int partnershipIdx = req.getPartnershipIdx();

        // 현재 구독(license_partnership) 조회: 없을 수 있음
        final var lp = licensePartnershipMapper.selectByPartnershipIdx(partnershipIdx).orElse(null);

        // 타겟 플랜은 필수
        final var targetPlan = licenseMapper.selectByIdx(req.getLicenseIdx())
                .orElseThrow(() -> new CustomException(ErrorCode.COMMON_EMPTY));

        final ZoneId Z = ZoneId.of("Asia/Seoul");
        final ZonedDateTime T = ZonedDateTime.now(Z);

        // ===== 기간 결정 =====
        final LocalDate periodStart;
        final LocalDate periodEndExcl;

        if (lp == null) {
            // 신규 구독 미보유: 정책 “결제는 오늘, 구독 효력은 내일 시작”
            final LocalDate tomorrow = T.toLocalDate().plusDays(1);
            periodStart = tomorrow;
            // 앵커 재사용 로직이 있다면 교체 가능. 기본값: +1개월(배타 끝)
            periodEndExcl = tomorrow.plusMonths(1);
        } else {
            periodStart   = lp.getPeriodStartDate();
            periodEndExcl = lp.getPeriodEndDate();
        }

        // ===== 직전 결제/인보이스 기준 미청구 산정 시작 시점 =====
        final var lastInv  = (lp != null)
                ? invoiceMapper.selectLastIssuedByLicensePartnershipIdx(lp.getIdx()).orElse(null)
                : null;

        final var lastPaid = (lastInv != null)
                ? licensePaymentHistoryMapper.selectByInvoiceIdxAndLastPaidDate(lastInv.getIdx()).orElse(null)
                : null;

        final ZonedDateTime baseFromTs =
                (lastPaid != null && lastPaid.getPaidDate() != null) ? lastPaid.getPaidDate()
                        : (lastInv != null && lastInv.getIssueDate() != null) ? lastInv.getIssueDate()
                        : periodStart.atStartOfDay(Z);

        final LocalDate baseFromDay = baseFromTs.withZoneSameInstant(Z).toLocalDate();

        // ===== 좌석 관련 스냅샷/활성값 =====
        final int active = partnershipMemberMapper.countByPartnershipIdxAndNotStateCd(
                partnershipIdx, EnumCode.PartnershipMember.StateCd.Delete.getCode());

        final Integer snapshotSeats = (lp != null) ? lp.getCurrentSeatCount() : null;

        // ===== 현재 플랜 조회(없을 수 있음) =====
        final var currentPlanOpt = (lp != null)
                ? licenseMapper.selectByIdx(lp.getLicenseIdx()).orElse(null)
                : null;

        // ===== 이벤트 로드 =====
        final List<ProrationInput.SeatEvent> seatEvents;
        {
            if (lp == null) {
                seatEvents = List.of(); // 구독이 없으면 이벤트도 없음
            } else {
                final LocalDate tomorrow = T.toLocalDate().plusDays(1);
                final var evts = subscriptionChangeEventMapper
                        .selectByLicensePartnershipIdxAndOccurredDate(lp.getIdx(), baseFromTs, tomorrow.atStartOfDay(Z));
                seatEvents = evts.stream()
                        .map(e -> ProrationInput.SeatEvent.builder()
                                .occurredAt(e.getOccurredDate().toLocalDate())
                                .delta(e.getQtyDelta())
                                .relatedId(e.getIdx().longValue())
                                .build())
                        .toList();
            }
        }

        // ===== 케이스 판정 =====
        final ProrationInput.CaseType caseType;
        final ProrationInput.Plan fromPlan;
        final ProrationInput.Plan toPlan;

        if (currentPlanOpt == null || isFreePlan(currentPlanOpt)) {
            caseType = ProrationInput.CaseType.NEW_TO_PAID;
            fromPlan = null;
            toPlan   = planOf(targetPlan);
        } else {
            final var currentPlan = currentPlanOpt;
            final int cmp = targetPlan.getPricePerUser().compareTo(currentPlan.getPricePerUser());
            if (cmp > 0) {
                caseType = ProrationInput.CaseType.UPGRADE;
            } else if (cmp < 0 || !currentPlan.getIdx().equals(targetPlan.getIdx())) {
                caseType = ProrationInput.CaseType.DOWNGRADE;
            } else {
                throw new CustomException(ErrorCode.COMMON_INVALID); // 동일 플랜
            }
            fromPlan = planOf(currentPlan);
            toPlan   = planOf(targetPlan);
        }

        // ===== 미청구 시작 좌석 산정 =====
        final int startingSeatsForAccrual;
        if (lp == null) {
            // 신규 구독: 직전 RECURRING 없음 → 0(또는 toPlan.min으로 바꾸려면 여기를 교체)
            startingSeatsForAccrual = 0;
        } else {
            startingSeatsForAccrual = resolveStartingSeatsForAccrual(
                    lastInv,
                    lp,
                    (fromPlan != null ? nz(fromPlan.getMinUserCount()) : 0)
            );
        }

        return ProrationInput.builder()
                .paymentTime(T)
                .zone(Z)
                .paidDate(baseFromDay)
                .periodStart(periodStart)
                .periodEndExcl(periodEndExcl)
                .fromPlan(fromPlan)
                .toPlan(toPlan)
                .snapshotSeats(snapshotSeats)
                .activeSeats(active)
                .useSnapshotFirst(true)
                .startingSeatsForAccrual(startingSeatsForAccrual)
                .seatEvents(seatEvents)
                .roundingMode(RoundingMode.HALF_UP)
                .currency("KRW")
                .caseType(caseType)
                .build();
    }

    private static ProrationInput.Plan planOf(LicenseVO p) {
        return ProrationInput.Plan.builder()
                .idx(p.getIdx())
                .planCd(p.getPlanCd())
                .name(p.getName())
                .pricePerUser(p.getPricePerUser())
                .minUserCount(p.getMinUserCount())
                .build();
    }

    private static boolean isFreePlan(LicenseVO p) {
        return p.getPricePerUser() == null || p.getPricePerUser().signum() == 0;
    }

    private int resolveStartingSeatsForAccrual(InvoiceVO lastInv, LicensePartnershipVO lp, int fallbackMin) {
        // 직전 RECURRING 라인 quantity → 없으면 fallbackMin
        Integer q = /* 당신의 기존 로직 */ resolvePrepaidSeatsFromLastRecurring(lastInv, lp);
        return q != null ? q : fallbackMin;
    }

    private int resolvePrepaidSeatsFromLastRecurring(InvoiceVO lastInv, LicensePartnershipVO lp) {
        if (lastInv != null) {
            var recurring = invoiceItemMapper.selectRecurringByInvoiceIdx(lastInv.getIdx()); // 구현 필요: 최신 인보이스의 item_type = RECURRING 1건 반환
            if (recurring != null && recurring.getQuantity() != null && recurring.getQuantity() > 0) {
                return recurring.getQuantity(); // ex) 3석
            }
        }
        // 폴백: 직전 결제 당시 스냅샷이 없으면 최소 과금 인원 사용
        return 0;
    }

    private static int nz(Integer v) { return v == null ? 0 : v; }
}
