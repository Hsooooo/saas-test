package com.illunex.emsaasrestapi.payment;

import com.illunex.emsaasrestapi.common.CustomException;
import com.illunex.emsaasrestapi.common.ErrorCode;
import com.illunex.emsaasrestapi.common.code.EnumCode;
import com.illunex.emsaasrestapi.license.mapper.LicenseMapper;
import com.illunex.emsaasrestapi.license.mapper.LicensePartnershipMapper;
import com.illunex.emsaasrestapi.license.vo.LicensePartnershipVO;
import com.illunex.emsaasrestapi.license.vo.LicenseVO;
import com.illunex.emsaasrestapi.partnership.PartnershipComponent;
import com.illunex.emsaasrestapi.payment.dto.PaymentPreviewResult;
import com.illunex.emsaasrestapi.payment.mapper.InvoiceItemMapper;
import com.illunex.emsaasrestapi.payment.mapper.InvoiceMapper;
import com.illunex.emsaasrestapi.payment.mapper.SubscriptionChangeEventMapper;
import com.illunex.emsaasrestapi.payment.util.ProrationEngine;
import com.illunex.emsaasrestapi.payment.util.ProrationInput;
import com.illunex.emsaasrestapi.payment.util.ProrationResult;
import com.illunex.emsaasrestapi.payment.vo.InvoiceVO;
import com.illunex.emsaasrestapi.payment.vo.SubscriptionChangeEventVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.time.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

import static com.illunex.emsaasrestapi.common.code.EnumCode.LicensePartnership.StateCd.CANCEL;
import static com.illunex.emsaasrestapi.common.code.EnumCode.LicensePartnership.StateCd.CHANGE;
import static com.illunex.emsaasrestapi.payment.util.ProrationComponent.planOf;
import static java.lang.Boolean.TRUE;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentSchedule {

    private final LicensePartnershipMapper lpMapper;
    private final InvoiceMapper invoiceMapper;
    private final SubscriptionChangeEventMapper eventMapper;
    private final InvoiceItemMapper invoiceItemMapper; // 현재는 안 쓰지만 남겨둠(추가 확장용)
    private final PaymentService paymentService;
    private final LicenseMapper licenseMapper;
    private final ProrationEngine prorationEngine;
    private final PartnershipComponent partnershipComponent;
    private final TossPaymentService tossPaymentService; // 자동결제 2단계 붙일 때 사용 예정

    @Scheduled(cron = "0 0 2 * * ?", zone = "Asia/Seoul")
    public List<PaymentPreviewResult> generateMonthlyInvoiceBatch() {
        List<Integer> targetIds = lpMapper.selectIdxsByNextBillingDateToday();
        List<PaymentPreviewResult> res = new ArrayList<>();
        for (Integer lpId : targetIds) {
            try {
                PaymentPreviewResult r = generateMonthlyInvoiceTransactional(lpId);
                if (r != null) {
                    res.add(r);
                }
            } catch (Exception e) {
                log.error("Monthly invoice failed for lpId={}", lpId, e);
            }
        }
        return res;
    }

    /**
     * 월간 인보이스 생성 트랜잭션
     * 1. 해지 예약
     *   1-1. 해당 구독 기간 중 좌석 변경분 미결금액 처리 (A)
     *   1-2. 신규 인보이스 생성 X
     * 2. 변경 예약
     *   2-1. 해당 구독 기간 중 좌석 변경분 미결금액 처리 (A)
     *   2-2. 변경된 구독 요금제로 신규 인보이스 생성
     * 3. 정기 결제
     *   3-1. 해당 구독 기간 중 좌석 변경분 미결금액 처리 (A)
     *   3-2. 정기 결제 신규 인보이스 생성
     *
     * A. 좌석 변경분 미결금액 처리
     *   - 현재 구독 기간 내에 업그레이드 이력 존재하는 경우 업그레이드 이후~종료일까지 정산
     */
    @Transactional
    PaymentPreviewResult generateMonthlyInvoiceTransactional(Integer lpId) throws CustomException {
        // 1) 대상 LP 잠금 조회
        final LicensePartnershipVO lp = lpMapper.selectByIdxForUpdate(lpId);
        if (lp == null) return null;

        final LicenseVO currentLicense = licenseMapper.selectByIdx(lp.getLicenseIdx())
                .orElse(null);
        if (currentLicense == null) {
            log.warn("No currentLicense for lpId={}", lpId);
            return null;
        }

        // 마지막 PAID 인보이스 기준
        final InvoiceVO lastPaidInvoice = invoiceMapper.selectLastPaidByLicensePartnershipIdx(lp.getIdx());
        if (lastPaidInvoice == null) {
            log.warn("No lastPaidInvoice for lpId={}", lpId);
            return null;
        }

        int currentUserCount = partnershipComponent.getPartnershipActiveMemberCount(lp.getPartnershipIdx());

        // 상태 플래그
        final boolean isCancelReserved = TRUE.equals(lp.getCancelAtPeriodEnd())
                || CANCEL.getCode().equals(lp.getStateCd());
        final boolean isChangeReserved = CHANGE.getCode().equals(lp.getStateCd());

        // 2) 좌석 변경분 정산 입력 + 결과
        ProrationInput prorationInput = buildProrationInput(currentLicense, lp, lastPaidInvoice, lp.getStateCd());
        ProrationResult prorationResult = prorationEngine.calculate2(prorationInput);
        PaymentPreviewResult prorationPreview = PaymentPreviewResult.of(prorationResult);
        prorationPreview.setPartnershipIdx(lp.getPartnershipIdx());

        log.info("ProrationResult for lpId={}: {}", lpId, prorationResult);
        log.info("PaymentPreviewResult (proration-only) for lpId={}: {}", lpId, prorationPreview);

        if (isCancelReserved) {
            return handleCancelReserved(lp, currentLicense, lastPaidInvoice, prorationPreview, currentUserCount);
        }

        if (isChangeReserved) {
            return handleChangeReserved(lp, currentLicense, lastPaidInvoice, prorationPreview, currentUserCount);
        }

        return handleRegularRecurring(lp, currentLicense, lastPaidInvoice, prorationPreview, currentUserCount);
    }

    /**
     * 1) 해지 예약: 좌석 변경분 조정 인보이스 생성 후 LP 종료
     */
    @Transactional
    protected PaymentPreviewResult handleCancelReserved(LicensePartnershipVO lp,
                                                        LicenseVO currentPlan,
                                                        InvoiceVO lastPaidInvoice,
                                                        PaymentPreviewResult prorationPreview,
                                                        int currentUserCount) {

        // 1-1) 좌석 변경분만 담긴 조정 인보이스 생성
        if (prorationPreview.getItems() != null && !prorationPreview.getItems().isEmpty()) {
            InvoiceVO adjInv = paymentService.upsertDraftOpenInvoice(lp.getIdx(), prorationPreview);
            log.info("Created adjust invoice (cancel) lpId={}, invoiceId={}", lp.getIdx(), adjInv.getIdx());

            // === 여기서 자동 결제 시도 ===
            try {
                paymentService.autoChargeByPreviewForBatch(lp, adjInv, prorationPreview);
            } catch (Exception e) {
                log.error("Auto charge failed for cancel lpId={}, invoiceId={}", lp.getIdx(), adjInv.getIdx(), e);
                // TODO: 실패 시 정책
                //  - lp를 SUSPEND로 보낼 것인지
                //  - 그냥 OPEN 인보이스만 남기고 사용자에게 알림을 줄 것인지
            }
        } else {
            log.info("No seat changes to adjust for cancel lpId={}", lp.getIdx());
        }

        // 1-2) LP 종료 처리 (결제 성공 여부에 따라 정책 조정 가능)
        // ex) 성공했을 때만 종료, 실패 시에는 해지 보류 등
//    lp.setStateCd(CANCEL.getCode());
//    lp.setNextBillingDate(null); // 더 이상 배치 대상 아님
//    lpMapper.updateByLicensePartnershipVO(lp);

        return prorationPreview;
    }

    /**
     * 2) 변경 예약:
     *  - 좌석 변경분 조정 인보이스
     *  - 변경된 플랜으로 다음 사이클 정기 인보이스 생성
     *  - LP 플랜/기간 롤오버
     */
    @Transactional
    protected PaymentPreviewResult handleChangeReserved(LicensePartnershipVO lp,
                                                        LicenseVO currentPlan,
                                                        InvoiceVO lastPaidInvoice,
                                                        PaymentPreviewResult prorationPreview,
                                                        int currentUserCount) throws CustomException {



        // 2-2) 변경될 플랜 조회
        // 현재는 lp.getTargetLicenseIdx() 가 있다고 가정.
        // 만약 없다면, eventMapper에서 PLAN_DOWNGRADE 이벤트의 toLicenseIdx를 찾아야 함.
        SubscriptionChangeEventVO downgradeEvent = eventMapper.selectLastOneByLpAndOccurredBetweenAndTypeCd(
                lp.getIdx(),
                lp.getPeriodStartDate().atStartOfDay(),
                LocalDateTime.of(lp.getPeriodEndDate(), LocalTime.MAX),
                EnumCode.SubscriptionChangeEvent.TypeCd.PLAN_DOWNGRADE.getCode()
        );
        if (downgradeEvent == null) {
            throw new CustomException(ErrorCode.COMMON_INVALID, "no PLAN_DOWNGRADE event found for CHANGE state");
        }
        Integer targetLicenseIdx = downgradeEvent.getToLicenseIdx();
        if (targetLicenseIdx == null) {
            throw new CustomException(ErrorCode.COMMON_INVALID, "target license not set for CHANGE state");
        }

        LicenseVO targetPlan = licenseMapper.selectByIdx(targetLicenseIdx)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMON_EMPTY, "target plan not found"));
        prorationPreview.setToLicenseIdx(targetPlan.getIdx());

        // 다음 기간 계산
        LocalDate nextPeriodStart = lastPaidInvoice.getPeriodEnd();
        LocalDate nextPeriodEnd   = calcNextPeriodEnd(lp.getBillingDay(), nextPeriodStart);
        prorationPreview.setPeriodStart(nextPeriodStart);
        prorationPreview.setPeriodEnd(nextPeriodEnd);

        int chargeSeats = Math.max(currentUserCount, targetPlan.getMinUserCount());
        long unitPrice  = targetPlan.getPricePerUser().longValueExact();
        long lineAmount = unitPrice * chargeSeats;

        PaymentPreviewResult.PreviewResultItem recurringItem =
                PaymentPreviewResult.PreviewResultItem.builder()
                        .itemType("RECURRING")
                        .description("Recurring seats x unit price (prepaid)")
                        .quantity(chargeSeats)
                        .unitPrice(unitPrice)
                        .days(null)
                        .amount(lineAmount)
                        .meta(new JSONObject()
                                .put("type", EnumCode.InvoiceItem.ItemTypeCd.RECURRING.getCode())
                                .put("plan_unit_price", targetPlan.getPricePerUser())
                                .put("charge_seats", chargeSeats)
                                .put("period_start", nextPeriodStart.toString())
                                .put("period_end", nextPeriodEnd.toString())
                                .toMap())
                        .build();

        // 조정 + 다음 기간 정기 라인을 하나의 인보이스로 묶고 싶으면 prorationPreview에 추가
        List<PaymentPreviewResult.PreviewResultItem> mergedItems = new ArrayList<>();

        // 기존 리스트가 있으면 내용만 복사
        if (prorationPreview.getItems() != null) {
            mergedItems.addAll(prorationPreview.getItems());
        }

        // 새 RECURRING 라인 추가
        mergedItems.add(recurringItem);

        // 새로 만든 가변 리스트를 다시 세팅
        prorationPreview.setItems(mergedItems);

        InvoiceVO inv = null;
        if (prorationPreview.getItems() != null && !prorationPreview.getItems().isEmpty()) {
            inv = paymentService.upsertDraftOpenInvoice(lp.getIdx(), prorationPreview);
            log.info("Created mixed invoice (change) lpId={}, invoiceId={}", lp.getIdx(), inv.getIdx());

            // === 여기서 자동 결제 시도 ===
            try {
                paymentService.autoChargeByPreviewForBatch(lp, inv, prorationPreview);
            } catch (Exception e) {
                log.error("Auto charge failed for change lpId={}, invoiceId={}", lp.getIdx(), inv.getIdx(), e);
                // TODO: 실패 시 정책
                //  - 변경 예약 유지 + dunning
                //  - 변경 롤백 등
            }
        }

        // LP 롤오버 + 플랜 교체 (결제 성공 시에만 반영하고 싶다면 위 try/catch 안에서 처리해도 됨)
        lp.setLicenseIdx(targetPlan.getIdx());
        lp.setPeriodStartDate(nextPeriodStart);
        lp.setPeriodEndDate(nextPeriodEnd);
        lp.setNextBillingDate(nextPeriodEnd);
        lp.setStateCd(EnumCode.LicensePartnership.StateCd.ACTIVE.getCode());
        lp.setCurrentSeatCount(currentUserCount);
        lp.setCurrentUnitPrice(targetPlan.getPricePerUser());
        lp.setCurrentMinUserCount(targetPlan.getMinUserCount());
        lpMapper.updateByLicensePartnershipVO(lp);

        return prorationPreview;
    }

    /**
     * 3) 일반 정기 결제:
     *  - 현재 기간의 좌석 변경분 조정 + 다음 기간 선불 RECURRING을 한 인보이스에 묶음
     *  - LP 기간 롤오버
     */
    @Transactional
    protected PaymentPreviewResult handleRegularRecurring(LicensePartnershipVO lp,
                                                          LicenseVO currentPlan,
                                                          InvoiceVO lastPaidInvoice,
                                                          PaymentPreviewResult prorationPreview,
                                                          int currentUserCount) throws CustomException {

        LocalDate nextPeriodStart = lastPaidInvoice.getPeriodEnd();
        LocalDate nextPeriodEnd   = calcNextPeriodEnd(lp.getBillingDay(), nextPeriodStart);

        int chargeSeats = Math.max(currentUserCount, currentPlan.getMinUserCount());
        long unitPrice  = currentPlan.getPricePerUser().longValueExact();
        long recurringAmount = unitPrice * chargeSeats;

        PaymentPreviewResult.PreviewResultItem recurringItem =
                PaymentPreviewResult.PreviewResultItem.builder()
                        .itemType("RECURRING")
                        .description("Recurring seats x unit price (prepaid)")
                        .quantity(chargeSeats)
                        .unitPrice(unitPrice)
                        .days(null)
                        .amount(recurringAmount)
                        .meta(new JSONObject()
                                .put("type", EnumCode.InvoiceItem.ItemTypeCd.RECURRING.getCode())
                                .put("plan_unit_price", currentPlan.getPricePerUser())
                                .put("charge_seats", chargeSeats)
                                .put("period_start", nextPeriodStart.toString())
                                .put("period_end", nextPeriodEnd.toString())
                                .toMap())
                        .build();

        List<PaymentPreviewResult.PreviewResultItem> allItems = new ArrayList<>();
        if (prorationPreview.getItems() != null) {
            allItems.addAll(prorationPreview.getItems());
        }
        allItems.add(recurringItem);

        long totalAmount = allItems.stream()
                .mapToLong(PaymentPreviewResult.PreviewResultItem::getAmount)
                .sum();

        PaymentPreviewResult mixedPreview = PaymentPreviewResult.builder()
                .items(allItems)
                .periodStart(nextPeriodStart)
                .periodEnd(nextPeriodEnd)
                .amount(Math.toIntExact(totalAmount))
                .fromLicenseIdx(currentPlan.getIdx())
                .toLicenseIdx(currentPlan.getIdx())
                .partnershipIdx(lp.getPartnershipIdx())
                .build();

        // 인보이스 생성
        InvoiceVO inv = paymentService.upsertDraftOpenInvoice(lp.getIdx(), mixedPreview);
        log.info("Created mixed (proration + recurring) invoice lpId={}, invoiceId={}", lp.getIdx(), inv.getIdx());

        // === 여기서 자동 결제 시도 ===
        try {
            paymentService.autoChargeByPreviewForBatch(lp, inv, mixedPreview);
        } catch (Exception e) {
            log.error("Auto charge failed for recurring lpId={}, invoiceId={}", lp.getIdx(), inv.getIdx(), e);
            // TODO: 실패 시 dunning 정책
            //  - lp 상태를 SUSPEND로 변경
            //  - nextBillingDate 그대로 두거나 한 주 뒤로 미루기 등
        }

        // LP 롤오버
        lp.setPeriodStartDate(nextPeriodStart);
        lp.setPeriodEndDate(nextPeriodEnd);
        lp.setNextBillingDate(nextPeriodEnd);
        lp.setCurrentSeatCount(currentUserCount);
        lpMapper.updateByLicensePartnershipVO(lp);

        return mixedPreview;
    }

    // ===================== 프레이션 입력 빌더 =====================

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

        // 2) 해지/변경 예약이 아니면 업그레이드 이전 이벤트 컷
        if (!CANCEL.getCode().equals(stateCd) && !CHANGE.getCode().equals(stateCd)) {
            events = filterEventList(events);
        }

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

    /**
     * PLAN_UPGRADE 이전 이벤트 컷:
     *  - 업그레이드 이후~종료일만 정산하기 위해 사용
     */
    private List<SubscriptionChangeEventVO> filterEventList(List<SubscriptionChangeEventVO> events) {
        events.sort(Comparator.comparing(SubscriptionChangeEventVO::getOccurredDate));

        int upgradeIdx = IntStream.range(0, events.size())
                .filter(i -> EnumCode.SubscriptionChangeEvent.TypeCd.PLAN_UPGRADE.getCode()
                        .equals(events.get(i).getTypeCd()))
                .findFirst()
                .orElse(-1);

        if (upgradeIdx > 0) {
            return events.subList(upgradeIdx, events.size());
        }
        return events;
    }

    /** billingDay(1~28) 기준 다음 주기의 종료일(= next billing date) 계산 */
    private LocalDate calcNextPeriodEnd(Integer billingDay, LocalDate nextPeriodStart) throws CustomException {
        YearMonth ym = YearMonth.from(nextPeriodStart);
        YearMonth ymNext = ym.plusMonths(1);
        int day = Math.min(billingDay, ymNext.lengthOfMonth());
        return LocalDate.of(ymNext.getYear(), ymNext.getMonth(), day);
    }

    // 디버깅용 테스트 메서드
    public void test() throws CustomException {
        LicensePartnershipVO lp = lpMapper.selectByIdx(1).orElse(null);
        if (lp == null) return;
        LicenseVO currentPlan = licenseMapper.selectByIdx(lp.getLicenseIdx()).orElse(null);
        if (currentPlan == null) return;
        InvoiceVO lastPaidInvoice = invoiceMapper.selectLastPaidByLicensePartnershipIdx(lp.getIdx());
        if (lastPaidInvoice == null) return;

        ProrationInput input = buildProrationInput(currentPlan, lp, lastPaidInvoice, lp.getStateCd());
        ProrationResult res = prorationEngine.calculate2(input);
        PaymentPreviewResult previewResult = PaymentPreviewResult.of(res);
        log.info("test previewResult: {}", previewResult);
    }
}
