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
import com.illunex.emsaasrestapi.payment.vo.SubscriptionChangeEventVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static com.illunex.emsaasrestapi.common.code.EnumCode.LicensePartnership.StateCd.CANCEL;
import static com.illunex.emsaasrestapi.common.code.EnumCode.LicensePartnership.StateCd.CHANGE;

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
    private final SubscriptionChangeEventMapper eventMapper;

    public ProrationInput buildInputForPreview(RequestPaymentDTO.SubscriptionInfo req) throws CustomException {
        final int partnershipIdx = req.getPartnershipIdx();

        // 현재 구독(license_partnership) 조회: 없을 수 있음
        final var lp = licensePartnershipMapper.selectByPartnershipIdx(partnershipIdx).orElse(null);

        // 타겟 플랜은 필수
        final var targetPlan = licenseMapper.selectByIdx(req.getLicenseIdx())
                .orElseThrow(() -> new CustomException(ErrorCode.COMMON_EMPTY));

        final ZoneId Z = ZoneId.of("Asia/Seoul");
        ZonedDateTime T;
        if (req.getCalcDate() == null) {
            T = ZonedDateTime.now(Z);
        } else {
            T = req.getCalcDate().withZoneSameInstant(Z);
        }

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
            } else if (cmp < 0 || !currentPlan.getIdx().equals(targetPlan.getIdx()) || !isFreePlan(targetPlan)) {
                caseType = ProrationInput.CaseType.DOWNGRADE;
            } else if (isFreePlan(targetPlan)) {
                caseType = ProrationInput.CaseType.CANCEL;
            } else {
                throw new CustomException(ErrorCode.COMMON_INVALID);
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
                .anchorDate(T.toLocalDate())
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

    public static ProrationInput.Plan planOf(LicenseVO p) {
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

    public static Map<String, Object> metaRecurring(LocalDate from, LocalDate toExcl, int chargeUserCount, String planCd,
                                                    LocalDate paymentDate) {
        return Map.of(
                "charge_user_count", chargeUserCount,      // oldMin
                "from", from.toString(),             // 세그먼트 시작
                "to", toExcl.toString(),             // 세그먼트 끝(배타)
                "planCd", planCd,                    // 구 플랜 코드
                "paymentDate", paymentDate.toString()  // 업그레이드 기준일
        );
    }

    /**
     * A. 좌석 변경분 미결금액 처리용 입력 빌더
     *   - 현재 구독 기간 내에 업그레이드 이력 존재 시, 업그레이드 이후~종료일만 정산하기 위해 filterEventList 적용
     */
    public ProrationInput buildProrationInput(LicenseVO currentPlan,
                                              LicensePartnershipVO lp,
                                              InvoiceVO lastPaidInvoice,
                                              String stateCd) throws CustomException {

        if (lastPaidInvoice == null) {
            throw new CustomException(ErrorCode.COMMON_INVALID, "lastPaidInvoice is null");
        }

        // 1) 이벤트 로딩 (해당 결제기간 기준)
        List<SubscriptionChangeEventVO> events = eventMapper.selectByLpAndOccurredBetween(
                lp.getIdx(),
                lastPaidInvoice.getPeriodStart(),
                lastPaidInvoice.getPeriodEnd()
        );

        // 3) SeatEvent 변환
        final List<ProrationInput.SeatEvent> seatEvents = events.stream()
                .map(e -> ProrationInput.SeatEvent.builder()
                        .occurredAt(e.getOccurredDate().toLocalDate())
                        .delta(e.getQtyDelta())
                        .relatedId(e.getIdx().longValue())
                        .build())
                .toList();

        ProrationInput.Plan fromPlan = planOf(currentPlan);

        // 4) caseType 결정
        //    - 현재 설계에서는 CANCEL을 "조정만(정기 라인 없음)" 의미로 사용
        ProrationInput.CaseType caseType = ProrationInput.CaseType.CANCEL;

        return ProrationInput.builder()
                .paidSeat(lastPaidInvoice.getChargeUserCount())
                .paidDate(lastPaidInvoice.getIssueDate().toLocalDate())
                .paymentTime(ZonedDateTime.now())
                .periodStart(lastPaidInvoice.getPeriodStart())
                .periodEndExcl(lastPaidInvoice.getPeriodEnd())
                .anchorDate(lastPaidInvoice.getPeriodEnd().minusDays(1)) // anchor < periodEndExcl 보장
                .snapshotSeats(lp.getCurrentSeatCount())
                .seatEvents(seatEvents)
                .fromPlan(fromPlan)
                .toPlan(fromPlan) // 동일 플랜 내 좌석 변경 정산
                .roundingMode(RoundingMode.HALF_UP)
                .currency("KRW")
                .caseType(caseType)
                .build();
    }
}
