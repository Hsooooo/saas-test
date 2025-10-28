package com.illunex.emsaasrestapi.payment;

import com.illunex.emsaasrestapi.common.CustomException;
import com.illunex.emsaasrestapi.common.ErrorCode;
import com.illunex.emsaasrestapi.common.code.EnumCode;
import com.illunex.emsaasrestapi.license.mapper.LicenseMapper;
import com.illunex.emsaasrestapi.license.mapper.LicensePartnershipMapper;
import com.illunex.emsaasrestapi.license.vo.LicensePartnershipVO;
import com.illunex.emsaasrestapi.license.vo.LicenseVO;
import com.illunex.emsaasrestapi.member.vo.MemberVO;
import com.illunex.emsaasrestapi.partnership.PartnershipComponent;
import com.illunex.emsaasrestapi.partnership.mapper.PartnershipMapper;
import com.illunex.emsaasrestapi.partnership.vo.PartnershipMemberVO;
import com.illunex.emsaasrestapi.partnership.vo.PartnershipVO;
import com.illunex.emsaasrestapi.payment.dto.RequestPaymentDTO;
import com.illunex.emsaasrestapi.payment.dto.ResponsePaymentDTO;
import com.illunex.emsaasrestapi.payment.mapper.*;
import com.illunex.emsaasrestapi.payment.util.ProrationEngine;
import com.illunex.emsaasrestapi.payment.util.ProrationInput;
import com.illunex.emsaasrestapi.payment.util.ProrationResult;
import com.illunex.emsaasrestapi.payment.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.math.RoundingMode.HALF_UP;
import static java.time.temporal.ChronoUnit.DAYS;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final InvoicePaymentViewMapper paymentMapper;
    private final InvoiceMapper invoiceMapper; // 재활용(필요시)
    private final LicensePartnershipMapper licensePartnershipMapper;
    private final LicenseMapper licenseMapper;
    private final PartnershipComponent partnershipComponent;
    private final PartnershipPaymentMethodMapper partnershipPaymentMethodMapper;
    private final TossPaymentService tossPaymentService;
    private final PaymentMandateMapper paymentMandateMapper;
    private final PartnershipMapper partnershipMapper;
    private final SubscriptionChangeEventMapper subscriptionChangeEventMapper;
    private final InvoiceItemMapper invoiceItemMapper;
    private final ProrationEngine prorationEngine;
//    private final PaymentProviderClient providerClient;

    /** 인보이스에 대해 기본 결제수단으로 즉시 결제 시도 */
    @Transactional
    public void chargeInvoice(Integer invoiceId) {
        // 1) 인보이스/요약 조회
        InvoicePaymentView view = paymentMapper.selectInvoicePaymentSummary(invoiceId);
        if (view == null) throw new IllegalArgumentException("invoice not found");
        BigDecimal invoiceTotal = view.getInvoiceTotal();
        BigDecimal paidTotal    = view.getPaidTotal();
        BigDecimal balanceDue   = invoiceTotal.subtract(paidTotal);

//        if (balanceDue.compareTo(BigDecimal.ZERO) <= 0) {
//            // 이미 전액 수납
//            paymentMapper.markPaid(invoiceId);
//            return;
//        }

        InvoiceVO inv = invoiceMapper.selectByIdx(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("invoice not found"));
        if (!"ICS0002".equals(inv.getStatusCd())) { // OPEN만 결제
            throw new IllegalStateException("invoice status must be OPEN");
        }

        // 2) 기본 결제수단/위임 조회
        Map<String,Object> method = paymentMapper.selectDefaultMethodAndMandate(inv.getPartnershipIdx());
        if (method == null) throw new IllegalStateException("no active default payment method/mandate");

        Integer paymentMethodIdx  = (Integer) method.get("paymentMethodIdx");
        Integer paymentMandateIdx = (Integer) method.get("paymentMandateIdx");
        String providerCd         = (String)  method.get("providerCd");

        // 3) attempt_no 계산
//        Integer maxAttempt = paymentMapper.selectMaxAttemptNo(invoiceId);
//        int nextAttemptNo = (maxAttempt == null ? 0 : maxAttempt) + 1;

        // 4) 결제시도 INSERT (PENDING)
        PaymentAttemptVO attempt = new PaymentAttemptVO();
        attempt.setInvoiceIdx(inv.getIdx());
        attempt.setPartnershipIdx(inv.getPartnershipIdx());
        attempt.setProviderCd(providerCd);
        attempt.setPaymentMethodIdx(paymentMethodIdx);
        attempt.setPaymentMandateIdx(paymentMandateIdx);
//        attempt.setAttemptNo(nextAttemptNo);
        attempt.setAmount(balanceDue);
        attempt.setUnitCd(inv.getUnitCd());
        attempt.setStatusCd("PAS0001"); // PENDING
        attempt.setOrderNumber(null);
        attempt.setMeta(null);
//        paymentMapper.insertPaymentAttempt(attempt);
//
//        // 5) PG 호출
//        PaymentProviderClient.ProviderChargeRequest req = new PaymentProviderClient.ProviderChargeRequest();
//        req.setProviderCd(providerCd);
//        req.setMandateId(loadMandateId(paymentMandateIdx)); // 별도 조회 함수
//        req.setCurrency("KRW");
//        req.setAmount(balanceDue);
//        req.setOrderReference("INV-" + inv.getIdx() + "-ATT-" + nextAttemptNo);
//
//        PaymentProviderClient.ProviderChargeResult res = providerClient.charge(req);
//
//        // 6) 성공/실패 처리
//        if (res.isSuccess()) {
//            // 6-1) attempt 업데이트
//            paymentMapper.updateAttemptResult(PaymentAttemptVO.builder()
//                    .idx(attempt.getIdx())
//                    .statusCd("PAS0002") // SUCCESS
//                    .orderNumber(res.getOrderNumber())
//                    .meta(res.getRawResponseJson())
//                    .build());
//
//            // 6-2) 수납 기록(중복 방지 UNIQUE(provider_cd, order_number))
//            LicensePaymentHistoryVO pay = new LicensePaymentHistoryVO();
//            pay.setInvoiceIdx(inv.getIdx());
//            pay.setProviderCd(providerCd);
//            pay.setOrderNumber(res.getOrderNumber());
//            pay.setAmount(balanceDue);
//            pay.setUnitCd(inv.getUnitCd());
//            pay.setMeta(res.getRawResponseJson());
//            paymentMapper.insertPaymentHistory(pay);
//
//            // 6-3) 전액 수납 여부 재확인 후 PAID 전환
//            Map<String, Object> after = paymentMapper.selectInvoicePaymentSummary(invoiceId);
//            BigDecimal remaining = ((BigDecimal) after.get("invoiceTotal"))
//                    .subtract((BigDecimal) after.get("paidTotal"));
//            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
//                paymentMapper.markPaid(invoiceId);
//            }
//        } else {
//            paymentMapper.updateAttemptResult(PaymentAttemptVO.builder()
//                    .idx(attempt.getIdx())
//                    .statusCd("PAS0003") // FAILED
//                    .failureCode(res.getFailureCode())
//                    .failureMessage(res.getFailureMessage())
//                    .meta(res.getRawResponseJson())
//                    .build());
//            // 예외는 삼켜 재시도 배치로 넘기거나, 여기서 던져도 무방(정책 선택)
//            throw new IllegalStateException("PG payment failed: " + res.getFailureMessage());
//        }
    }

    private String loadMandateId(Integer paymentMandateIdx) {
        // payment_mandate.mandate_id 조회용 Mapper 만들어 사용
        // 간단히 Repository/Mapper 호출한다고 가정
        return "...";
    }

    public void subscribe() {
        // 1) 검증
        LicenseVO license = licenseMapper.selectByIdx(1).orElseThrow(() -> new IllegalArgumentException("license not found"));
        if (license == null) throw new IllegalArgumentException("license not found");
        if (!license.getActive()) throw new IllegalStateException("license is not active");

        Integer minUsers = license.getMinUserCount();
        BigDecimal unitPrice = license.getPricePerUser();

//        // 2) 현재 LP 잠금 조회
//        Map<String, Object> currentLp = licensePartnershipMapper.selectActiveByPartnershipForUpdate(req.getPartnershipIdx());
//
//        // basic -> paid 전환 혹은 신규 생성
//        int requestedSeats = req.getSeats() == null ? minUsers : req.getSeats();
//        int chargeSeats = Math.max(requestedSeats, minUsers);
//
//        LocalDate today = LocalDate.now();
//        int anchorDay = "IMMEDIATE".equalsIgnoreCase(req.getStartMode())
//                ? Math.min(today.getDayOfMonth(), 28)
//                : (req.getAnchorDay() == null ? Math.min(today.getDayOfMonth(), 28) : req.getAnchorDay());
//
//        LocalDate nextAnchor = nextAnchorAfter(today, anchorDay);
//        LocalDate periodStart = "IMMEDIATE".equalsIgnoreCase(req.getStartMode()) ? today : nextAnchor;
//        LocalDate periodEnd   = "IMMEDIATE".equalsIgnoreCase(req.getStartMode()) ? nextAnchor : nextAnchor.plusMonths(1);
//
//        Integer lpIdx;
//        if (currentLp == null) {
//            // 신규 LP 생성
//            licensePartnershipMapper.insertForSubscribe(req.getPartnershipIdx(), (Integer)license.get("idx"),
//                    anchorDay, periodStart, periodEnd, chargeSeats, unitPrice);
//            lpIdx = licensePartnershipMapper.getLastInsertId();
//        } else {
//            // BASIC -> PAID 업데이트 (간단 판정: current_lp.license_idx 가 0원 요금제인지 여부는 운영 로직에 맞게 보완)
//            licensePartnershipMapper.updateForSubscribe(
//                    (Integer) currentLp.get("idx"),
//                    (Integer)license.get("idx"),
//                    anchorDay, periodStart, periodEnd, chargeSeats, unitPrice
//            );
//            lpIdx = (Integer) currentLp.get("idx");
//        }
//
//        // 이벤트 기록 (PLAN_UPGRADE)
//        seatEventMapper.insertPlanUpgrade(lpIdx, (Integer)license.get("idx"),
//                chargeSeats, req.getMeta() != null ? req.getMeta().getNote() : null);
//
//        SubscribeResponse res = new SubscribeResponse();
//        res.setLicensePartnershipIdx(lpIdx);
//
//        // IMMEDIATE -> PRORATION 인보이스 + 결제
//        if ("IMMEDIATE".equalsIgnoreCase(req.getStartMode())) {
//            int denominator = periodStart.lengthOfMonth();
//            int numerator = (int) ChronoUnit.DAYS.between(today, nextAnchor);
//            BigDecimal proration = unitPrice
//                    .multiply(BigDecimal.valueOf(chargeSeats))
//                    .multiply(BigDecimal.valueOf(numerator))
//                    .divide(BigDecimal.valueOf(denominator), java.math.RoundingMode.HALF_UP);
//
//            // invoice 생성
//            invoiceMapper.insertOpen(req.getPartnershipIdx(), lpIdx, periodStart, periodEnd);
//            Integer invoiceIdx = invoiceMapper.getLastInsertId();
//
//            // item 생성 (PRORATION)
//            Map<String, Object> meta = new HashMap<>();
//            meta.put("basis", req.getProrationBasis());
//            meta.put("denominator", denominator);
//            meta.put("numerator", numerator);
//            meta.put("unitPrice", unitPrice);
//            meta.put("chargeSeats", chargeSeats);
//            invoiceItemMapper.insertProration(invoiceIdx, chargeSeats, unitPrice, numerator, proration, meta);
//
//            // 합계 업데이트
//            invoiceMapper.updateTotals(invoiceIdx);
//
//            // 결제 시도
//            Integer attemptIdx = paymentAttemptMapper.insertInitiated(invoiceIdx, proration,
//                    req.getPayment() != null ? req.getPayment().getProviderCd() : null,
//                    req.getPayment() != null ? req.getPayment().getPaymentMethodIdx() : null,
//                    req.getPayment() != null ? req.getPayment().getPaymentMandateIdx() : null
//            );
//
//            PaymentGatewayClient.Result pg = paymentGatewayClient.capture(proration, req);
//            if (pg.isSuccess()) {
//                paymentAttemptMapper.updateCaptured(attemptIdx, pg.getOrderNumber());
//                paymentHistoryMapper.insertReceipt(invoiceIdx, pg.getProviderCd(), pg.getOrderNumber(), proration);
//                invoiceMapper.updateStatusPaid(invoiceIdx);
//            } else {
//                paymentAttemptMapper.updateFailed(attemptIdx, pg.getFailureCode(), pg.getFailureMessage());
//                // 인보이스는 OPEN 유지
//            }
//
//            // 응답 채우기(간단)
//            SubscribeResponse.Invoice inv = new SubscribeResponse.Invoice();
//            inv.setInvoiceIdx(invoiceIdx);
//            inv.setStatus("OPEN"); // 실제로는 쿼리로 다시 읽어 세팅 권장
//            SubscribeResponse.Period per = new SubscribeResponse.Period();
//            per.setStart(periodStart);
//            per.setEnd(periodEnd);
//            inv.setPeriod(per);
//            inv.setTotal(proration);
//            res.setInvoice(inv);
//        }
//
//        res.setNextBillingDate(nextAnchor);
    }

    private LocalDate nextAnchorAfter(LocalDate today, int anchorDay) {
        int day = Math.min(Math.max(anchorDay, 1), 28);
        LocalDate anchorThisMonth = today.withDayOfMonth(Math.min(day, today.lengthOfMonth()));
        if (!today.isBefore(anchorThisMonth)) {
            // today >= anchorThisMonth -> next month
            LocalDate nextMonth = today.plusMonths(1);
            return nextMonth.withDayOfMonth(Math.min(day, nextMonth.lengthOfMonth()));
        }
        return anchorThisMonth;
    }

    /**
     * 결제수단 등록
     * @param request
     * @param memberVO
     * @throws CustomException
     */
    @Transactional
    public void registerPaymentMethodToss(RequestPaymentDTO.MethodRegister request, MemberVO memberVO) throws CustomException, IOException {
        PartnershipMemberVO partnershipMember = partnershipComponent.checkPartnershipMember(memberVO, request.getPartnershipIdx());
        // TODO 결제 권한 보유여부 체크

        String billingKey = tossPaymentService.issueBillingKey(request.getCustomerKey(), request.getAuthKey());

        PartnershipPaymentMethodVO methodVO = new PartnershipPaymentMethodVO();
        methodVO.setPartnershipIdx(request.getPartnershipIdx());
        methodVO.setAuthKey(request.getAuthKey());
        methodVO.setCustomerKey(request.getCustomerKey());
        methodVO.setMethodTypeCd("PMC0001"); // 카드
        methodVO.setStateCd("PMS0001"); // 활성
        methodVO.setIsDefault(true);
        methodVO.setLast4(request.getLast4());
        methodVO.setBrand(request.getCardBrand());
        partnershipPaymentMethodMapper.insertByPartnershipPaymentMethodVO(methodVO);

        PaymentMandateVO mandateVO = new PaymentMandateVO();
        mandateVO.setPartnershipIdx(request.getPartnershipIdx());
        mandateVO.setPaymentMethodIdx(methodVO.getIdx());
        mandateVO.setProviderCd("TOSS");
        mandateVO.setMandateId(billingKey);
        mandateVO.setStatusCd("MDS0001"); // 활성
        paymentMandateMapper.insertByPaymentMandateVO(mandateVO);

    }

    public void newSubscriptionByMethod() {
        // 1) 검증

        // 2) LP 잠금 조회

        // 3) LP 생성/업데이트

        // 4) 이벤트 기록

        // 5) IMMEDIATE -> PRORATION 인보이스 + 결제

    }

//    public Long calculateProrationAmount(Integer partnershipIdx, RequestPaymentDTO.SubscriptionInfo req, MemberVO memberVO) throws CustomException {
//        PartnershipVO partnershipVO = partnershipMapper.selectByIdx(req.getPartnershipIdx())
//                .orElseThrow(() -> new CustomException(ErrorCode.COMMON_EMPTY));
//        partnershipComponent.checkPartnershipMember(memberVO, partnershipIdx);
//        LicenseVO licenseVO = licenseMapper.selectByIdx(req.getPartnershipIdx())
//                .orElseThrow(() -> new CustomException(ErrorCode.COMMON_EMPTY));
//
//        // 없으면 베이직 라이센스
//        Optional<LicensePartnershipVO> lpOpt = licensePartnershipMapper.selectByPartnershipIdx(req.getPartnershipIdx());
//
//        // 신규 구독이라 일할계산 필요 없음
//        if (!lpOpt.isPresent()) {
//            return 0L;
//        } else {
//            LicensePartnershipVO lp = lpOpt.get();
//            // 현재 LP 잠금 조회
//            Map<String, Object> currentLp = licensePartnershipMapper.selectActiveByPartnershipForUpdate(partnershipIdx);
//
//            // basic -> paid 전환 혹은 신규 생성
//            int chargeSeats = lp.getUserCount();
//
//            LocalDate today = LocalDate.now();
//            int anchorDay = lp.getBillingDay();
//
//            LocalDate nextAnchor = nextAnchorAfter(today, anchorDay);
//            LocalDate periodStart = nextAnchor;
//            LocalDate periodEnd   = nextAnchor.plusMonths(1);
//
//            // IMMEDIATE -> PRORATION 인보이스 + 결제
//            int denominator = periodStart.lengthOfMonth();
//            int numerator = (int) DAYS.between(today, nextAnchor);
//            BigDecimal proration = lp.getPricePerUser()
//                    .multiply(BigDecimal.valueOf(chargeSeats))
//                    .multiply(BigDecimal.valueOf(numerator))
//                    .divide(BigDecimal.valueOf(denominator), HALF_UP);
//
//            return proration.longValue();
//        }
//
//    }

    @Transactional(readOnly = true)
    public Object calculateProrationAmount(RequestPaymentDTO.SubscriptionInfo req, MemberVO memberVO) throws CustomException {
        ProrationInput input = buildInputForPreview(req, memberVO); // 아래 별도 메서드
        ProrationResult result = prorationEngine.calculate(input);

        // 2) DTO 매핑(현재 preview 응답 형식)
        return result;
//        int partnershipIdx = req.getPartnershipIdx();
//
//        // 0) 검증
//        var p = partnershipMapper.selectByIdx(partnershipIdx)
//                .orElseThrow(() -> new CustomException(ErrorCode.COMMON_EMPTY));
//        partnershipComponent.checkPartnershipMember(memberVO, partnershipIdx);
//
//        final String action = req.getAction(); // UPGRADE | DOWNGRADE | CANCEL
//        final String effective = (req.getEffective() == null) ? "NOW" : req.getEffective();
//        final boolean isUpgrade = "UPGRADE".equals(action);
//        final boolean isUpgradeNow = isUpgrade && !"PERIOD_END".equals(effective);
//
//        final ZonedDateTime occurredAt = ZonedDateTime.now();
//        final LocalDate today = occurredAt.toLocalDate();
//
//        LicenseVO toPlan = null;
//        if (!"CANCEL".equals(action)) {
//            toPlan = licenseMapper.selectByIdx(req.getLicenseIdx())
//                    .orElseThrow(() -> new CustomException(ErrorCode.COMMON_EMPTY));
//        }
//
//        // 1) 현재 구독(필수)
//        var lpOpt = licensePartnershipMapper.selectByPartnershipIdx(partnershipIdx);
//        if (lpOpt.isEmpty()) return buildFreeToPaidPreview(partnershipIdx, toPlan); // 무료→유료 시작
//        var lp = lpOpt.get();
//        if (!List.of(EnumCode.LicensePartnership.StateCd.ACTIVE.getCode(),
//                EnumCode.LicensePartnership.StateCd.PAUSED.getCode()).contains(lp.getStateCd())) {
//            return emptyNoChargePreview(partnershipIdx, lp, "구독 비활성 상태입니다.");
//        }
//
//        // 2) 기간/분모/분자
//        LocalDate start = lp.getPeriodStartDate();  // inclusive
//        LocalDate endExcl = lp.getPeriodEndDate();  // exclusive
//        int D = (int) DAYS.between(start, endExcl);
//        if (D <= 0) return emptyNoChargePreview(partnershipIdx, lp, "유효한 청구 주기가 아닙니다.");
//
//        int dRemain = Math.max(0, (int) DAYS.between(today, endExcl));
//        final LocalDate capEnd = isUpgradeNow ? today : endExcl; // 업그레이드 NOW면 업그레이드 일자까지 캡
//
//        // 3) 플랜/좌석 스냅샷
//        var fromPlan = licenseMapper.selectByIdx(lp.getLicenseIdx())
//                .orElseThrow(() -> new CustomException(ErrorCode.COMMON_EMPTY));
//
//        // 현재 과금 기준 좌석(현시점)
//        int currentActiveSeats = partnershipComponent.getPartnershipActiveMemberCount(partnershipIdx);
//        int minSeats = lp.getCurrentMinUserCount();
//        int chargeSeatsNow = Math.max(currentActiveSeats, minSeats);
//
//        var items = new ArrayList<ResponsePaymentDTO.PreviewItem>();
//        BigDecimal subTotal = BigDecimal.ZERO;
//        final BigDecimal Dbd = BigDecimal.valueOf(D);
//        final Function<BigDecimal, Long> KRW = bd -> bd.setScale(0, RoundingMode.HALF_UP).longValueExact();
//
//        // 직전 인보이스(선불 좌석 수 확인)
//        var lastInv = invoiceMapper.selectLastIssuedByLicensePartnershipIdx(lp.getIdx()).orElse(null);
//        int baseSeatsPrepaid = resolvePrepaidSeatsFromLastRecurring(lastInv, lp); // 없으면 minSeats로 폴백
//
//        // PERIOD_END 분기: 정보만
//        if (!isUpgradeNow) {
//            return infoOnlyPreview(partnershipIdx, lp, fromPlan, toPlan, start, endExcl, D, chargeSeatsNow,
//                    isUpgrade ? "업그레이드는 NOW 적용 시에만 즉시 정산됩니다. PERIOD_END는 다음 결제일부터 새 플랜이 적용됩니다."
//                            : ("DOWNGRADE".equals(action)
//                            ? "다운그레이드는 다음 결제일부터 적용됩니다. 남은기간 크레딧은 다음 청구서에서 상계됩니다."
//                            : "해지는 다음 결제일에 종료됩니다."));
//        }
//
//        // 4) 세그먼트 기반 미청구 + 업그레이드 계산
//        // 4-1) 세그먼트 경계
//        LocalDate baseFrom = (lastInv != null) ? lastInv.getIssueDate().toLocalDate() : start;
//        if (baseFrom.isBefore(start)) baseFrom = start; // 안전
//        if (!capEnd.isAfter(baseFrom)) {
//            // 업그레이드가 직전 인보이스 이전이면 미청구 없음
//            baseFrom = capEnd;
//        }
//
//        // 이벤트 로드: baseFrom ~ capEnd 사이 좌석 변동/플랜변경
//        List<SubscriptionChangeEventVO> evts = subscriptionChangeEventMapper.selectByLpAndOccurredBetween(lp.getIdx(), baseFrom, capEnd);
//        // deltaByDate 집계(동일일자 다건 합산)
//        Map<LocalDate, Integer> deltaByDate = evts.stream()//                .collect(Collectors.groupingBy(e -> e.getOccurredDate().toLocalDate(), TreeMap::new,
//                        Collectors.summingInt(SubscriptionChangeEventVO::getQtyDelta)));
//
//        // 브레이크포인트
//        TreeSet<LocalDate> bps = new TreeSet<>();
//        bps.add(baseFrom);
//        bps.add(capEnd);
//        deltaByDate.keySet().forEach(bps::add);
//
//        // 러닝 좌석 (baseFrom에서 시작). 선불 좌석을 베이스로 시작
//        int runningSeats = baseSeatsPrepaid;
//        BigDecimal unbilledOldPlan = BigDecimal.ZERO;
//        int newPlanRemainDays = Math.max(0, (int) DAYS.between(today, endExcl)); // 업그레이드 이후 잔여 총일수
//
//        LocalDate[] pts = bps.toArray(new LocalDate[0]);
//        for (int i = 0; i < pts.length - 1; i++) {
//            LocalDate segStart = pts[i];
//            LocalDate segEndExcl = pts[i + 1];
//            int days = (int) DAYS.between(segStart, segEndExcl);
//            if (days <= 0) continue;
//
//            // 세그먼트 시작에 이벤트가 있으면 먼저 적용(당일 00:00 효력)
//            Integer dlt = deltaByDate.get(segStart);
//            if (dlt != null) runningSeats += dlt;
//
//            int effectiveSeats = Math.max(runningSeats, minSeats);
//
//            // 업그레이드 이전 세그먼트: 미청구(ADD분만)
//            if (segEndExcl.isAfter(today)) {
//                // 업그레이드 이후 구간은 여기서 처리하지 않음 (신 플랜 전액은 아래에서 일괄 처리)
//                continue;
//            }
//
//            int addAbovePrepaid = Math.max(0, effectiveSeats - baseSeatsPrepaid);
//            if (addAbovePrepaid > 0) {
//                BigDecimal amt = fromPlan.getPricePerUser()
//                        .multiply(BigDecimal.valueOf(addAbovePrepaid))
//                        .multiply(BigDecimal.valueOf(days))
//                        .divide(Dbd, 0, RoundingMode.HALF_UP);
//                unbilledOldPlan = unbilledOldPlan.add(amt);
//                items.add(item("PRORATION", "좌석 변경 미청구분(구 플랜)",
//                        addAbovePrepaid, fromPlan.getPricePerUser().longValue(), days, KRW.apply(amt),
//                        null, Map.of("numerator", days, "denominator", D,
//                                "from", segStart.toString(), "to", segEndExcl.toString(),
//                                "planCd", fromPlan.getPlanCd())));
//            }
//        }
//
//        // 4-2) 업그레이드 NOW: 신 플랜 잔여기간 전액(현좌석) - 구 플랜 잔여기간 크레딧(선불 좌석)
//        if (dRemain > 0 && toPlan != null) {
//            BigDecimal newRemainAll = toPlan.getPricePerUser()
//                    .multiply(BigDecimal.valueOf(chargeSeatsNow))
//                    .multiply(BigDecimal.valueOf(dRemain))
//                    .divide(Dbd, 0, RoundingMode.HALF_UP);
//            items.add(item("PRORATION", "신 플랜 잔여기간(업그레이드 NOW)",
//                    chargeSeatsNow, toPlan.getPricePerUser().longValue(), dRemain, KRW.apply(newRemainAll),
//                    null, Map.of("numerator", dRemain, "denominator", D, "planCd", toPlan.getPlanCd())));
//            subTotal = subTotal.add(newRemainAll);
//
//            BigDecimal oldRemainCredit = fromPlan.getPricePerUser()
//                    .multiply(BigDecimal.valueOf(baseSeatsPrepaid))
//                    .multiply(BigDecimal.valueOf(dRemain))
//                    .divide(Dbd, 0, RoundingMode.HALF_UP)
//                    .negate();
//            if (baseSeatsPrepaid > 0) {
//                items.add(item("CREDIT", "구 플랜 남은기간 크레딧(선불 좌석)",
//                        baseSeatsPrepaid, fromPlan.getPricePerUser().longValue(), dRemain, KRW.apply(oldRemainCredit),
//                        null, Map.of("numerator", dRemain, "denominator", D, "planCd", fromPlan.getPlanCd())));
//                subTotal = subTotal.add(oldRemainCredit);
//            }
//        }
//
//        // 4-3) 구 플랜 미청구 합산 반영
//        subTotal = subTotal.add(unbilledOldPlan);
//
//        // 5) 합계
//        long subtotal = KRW.apply(subTotal);
//        long tax = 0L;
//        long total = subtotal + tax;
//        long creditCarry = (total < 0) ? Math.abs(total) : 0L;
//
//        return ResponsePaymentDTO.PaymentPreview.builder()
//                .partnershipIdx(partnershipIdx)
//                .licensePartnershipIdx(lp.getIdx())
//                .currentPlan(planSnap(fromPlan))
//                .targetPlan(toPlan != null ? planSnap(toPlan) : null)
//                .periodStart(start)
//                .periodEnd(endExcl)
//                .denominatorDays(D)
//                .occurredAt(occurredAt.toString())
//                .chargeSeats(chargeSeatsNow)
//                .roundingRule("HALF_UP")
//                .currency("KRW")
//                .items(items)
//                .subTotal(subtotal)
//                .tax(tax)
//                .total(total)
//                .willChargeNow(isUpgradeNow && total != 0)
//                .creditCarryOver(creditCarry)
//                .notes(List.of())
//                .build();
    }


    private ResponsePaymentDTO.PreviewPlan planSnap(LicenseVO plan) {
        ResponsePaymentDTO.PreviewPlan snap = new ResponsePaymentDTO.PreviewPlan();
        snap.setIdx(plan.getIdx());
        snap.setPlanCd(plan.getPlanCd());
        snap.setName(plan.getName());
        snap.setPricePerUser(plan.getPricePerUser().intValue());
        snap.setMinUserCount(plan.getMinUserCount());
        snap.setDataTotalLimit(plan.getDataTotalLimit());
        snap.setProjectCountLimit(plan.getProjectCountLimit());
        return snap;
    }

    private ResponsePaymentDTO.PreviewItem item(String type, String desc, int qty, long unit, int days, long amount,
                                                Long relatedId, Map<String,Object> meta) {
        var it = new ResponsePaymentDTO.PreviewItem();
        it.setItemType(type); it.setDescription(desc);
        it.setQuantity(qty); it.setUnitPrice(unit);
        it.setDays(days); it.setAmount(amount);
        it.setRelatedEventId(relatedId);
        it.setMeta(meta);
        return it;
    }

    private ResponsePaymentDTO.PaymentPreview emptyNoChargePreview(Integer partnershipIdx, LicensePartnershipVO lp, String note) {
        return ResponsePaymentDTO.PaymentPreview.builder()
                .partnershipIdx(partnershipIdx)
                .licensePartnershipIdx(lp.getIdx())
                .currentPlan(null)
                .targetPlan(null)
                .periodStart(lp.getPeriodStartDate())
                .periodEnd(lp.getPeriodEndDate())
                .denominatorDays((int) DAYS.between(lp.getPeriodStartDate(), lp.getPeriodEndDate()))
                .occurredAt(ZonedDateTime.now().toString())
                .chargeSeats(Math.max(lp.getCurrentSeatCount(), lp.getCurrentMinUserCount()))
                .roundingRule("HALF_UP")
                .currency("KRW")
                .items(List.of())
                .subTotal(0L).tax(0L).total(0L)
                .willChargeNow(false)
                .creditCarryOver(0L)
                .notes((note == null) ? List.of() : List.of(note))
                .build();
    }

    private ResponsePaymentDTO.PaymentPreview infoOnlyPreview(Integer partnershipIdx, LicensePartnershipVO lp,
                                                              LicenseVO fromPlan, LicenseVO toPlan,
                                                              LocalDate start, LocalDate endExcl, int D, int chargeSeats,
                                                              String note) {
        return ResponsePaymentDTO.PaymentPreview.builder()
                .partnershipIdx(partnershipIdx)
                .licensePartnershipIdx(lp.getIdx())
                .currentPlan(fromPlan != null ? planSnap(fromPlan) : null)
                .targetPlan(toPlan != null ? planSnap(toPlan) : null)
                .periodStart(start)
                .periodEnd(endExcl)
                .denominatorDays(D)
                .occurredAt(ZonedDateTime.now().toString())
                .chargeSeats(chargeSeats)
                .roundingRule("HALF_UP")
                .currency("KRW")
                .items(List.of())
                .subTotal(0L).tax(0L).total(0L)
                .willChargeNow(false)
                .creditCarryOver(0L)
                .notes(List.of(note))
                .build();
    }

    private ResponsePaymentDTO.PaymentPreview buildFreeToPaidPreview(Integer partnershipIdx, LicenseVO toPlan) {
        return ResponsePaymentDTO.PaymentPreview.builder()
                .partnershipIdx(partnershipIdx)
                .licensePartnershipIdx(null)
                .currentPlan(null)
                .targetPlan(toPlan != null ? planSnap(toPlan) : null)
                .periodStart(null)
                .periodEnd(null)
                .denominatorDays(null)
                .occurredAt(ZonedDateTime.now().toString())
                .chargeSeats(toPlan != null ? toPlan.getMinUserCount() : 0)
                .roundingRule("HALF_UP")
                .currency("KRW")
                .items(List.of())
                .subTotal(0L).tax(0L).total(0L)
                .willChargeNow(false)
                .creditCarryOver(0L)
                .notes(List.of("무료 → 유료 전환은 일할이 없고, 결제 시 다음 주기 선불로 청구됩니다."))
                .build();
    }

    private int resolvePrepaidSeatsFromLastRecurring(InvoiceVO lastInv, LicensePartnershipVO lp) {
        if (lastInv != null) {
            var recurring = invoiceItemMapper.selectRecurringByInvoiceIdx(lastInv.getIdx()); // 구현 필요: 최신 인보이스의 item_type = RECURRING 1건 반환
            if (recurring != null && recurring.getQuantity() != null && recurring.getQuantity() > 0) {
                return recurring.getQuantity(); // ex) 3석
            }
        }
        // 폴백: 직전 결제 당시 스냅샷이 없으면 최소 과금 인원 사용
        return Math.max(lp.getCurrentMinUserCount(), partnershipComponent.getPartnershipActiveMemberCount(lp.getPartnershipIdx()));
    }


    private ProrationInput buildInputForPreview(RequestPaymentDTO.SubscriptionInfo req, MemberVO member) throws CustomException {
        int partnershipIdx = req.getPartnershipIdx();
        var lp = licensePartnershipMapper.selectByPartnershipIdx(partnershipIdx)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMON_EMPTY));
        var fromPlan = licenseMapper.selectByIdx(lp.getLicenseIdx())
                .orElseThrow(() -> new CustomException(ErrorCode.COMMON_EMPTY));
        var toPlan   = licenseMapper.selectByIdx(req.getLicenseIdx())
                .orElseThrow(() -> new CustomException(ErrorCode.COMMON_EMPTY));

        var lastInv  = invoiceMapper.selectLastIssuedByLicensePartnershipIdx(lp.getIdx()).orElse(null);
        int prepaidSeats = resolvePrepaidSeatsFromLastRecurring(lastInv, lp);

        LocalDate start = lp.getPeriodStartDate();
        LocalDate endExcl = lp.getPeriodEndDate();
        LocalDate today = ZonedDateTime.now().toLocalDate();

        // baseFrom: lastInv.issueDate or periodStart
        LocalDate baseFrom = (lastInv != null) ? lastInv.getIssueDate().toLocalDate() : start;
        // capEnd: UPGRADE NOW → today, 배치/정기 → endExcl
        boolean isUpgradeNow = "UPGRADE".equals(req.getAction()) && !"PERIOD_END".equals(req.getEffective());
        LocalDate capEnd = isUpgradeNow ? today : endExcl;

        // 좌석 이벤트
        var evts = subscriptionChangeEventMapper.selectByLpAndOccurredBetween(lp.getIdx(), baseFrom, capEnd);
        List<ProrationInput.SeatEvent> seatEvents = evts.stream()
                .map(e -> ProrationInput.SeatEvent.builder()
                        .date(e.getOccurredDate().toLocalDate())
                        .delta(e.getQtyDelta())
                        .relatedId(e.getIdx().longValue())
                        .build())
                .toList();

        // 활성 좌석 & 스냅샷
        int active = partnershipComponent.getPartnershipActiveMemberCount(partnershipIdx);
        Integer snapshotSeats = lp.getCurrentSeatCount(); // null 가능

        return ProrationInput.builder()
                .periodStart(start)
                .periodEndExcl(endExcl)
                .today(today)
                .denominatorDays((int) DAYS.between(start, endExcl)) // 보통 31
                .roundingMode(RoundingMode.HALF_UP)
                .currency("KRW")
                .fromPlan(ProrationInput.Plan.builder()
                        .idx(fromPlan.getIdx())
                        .planCd(fromPlan.getPlanCd())
                        .pricePerUser(fromPlan.getPricePerUser())
                        .minUserCount(fromPlan.getMinUserCount())
                        .build())
                .toPlan(ProrationInput.Plan.builder()
                        .idx(toPlan.getIdx())
                        .planCd(toPlan.getPlanCd())
                        .pricePerUser(toPlan.getPricePerUser())
                        .minUserCount(toPlan.getMinUserCount())
                        .build())
                .prepaidSeats(prepaidSeats)
                .minChargeSeats(fromPlan.getMinUserCount())
                .currentActiveSeats(active)
                .useSnapshotSeatsFirst(true)  // 프리뷰 재현성 ↑
                .snapshotSeats(snapshotSeats)
                .action(ProrationInput.Action.valueOf(req.getAction()))
                .effective("NOW".equals(req.getEffective()) ? ProrationInput.Effective.NOW : ProrationInput.Effective.PERIOD_END)
                .seatEvents(seatEvents)
                .baseFrom(baseFrom)
                .capEnd(capEnd)
                .build();
    }

}
