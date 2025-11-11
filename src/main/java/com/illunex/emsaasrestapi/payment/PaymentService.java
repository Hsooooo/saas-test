package com.illunex.emsaasrestapi.payment;

import com.illunex.emsaasrestapi.common.CustomException;
import com.illunex.emsaasrestapi.common.ErrorCode;
import com.illunex.emsaasrestapi.common.code.EnumCode;
import com.illunex.emsaasrestapi.license.mapper.LicenseMapper;
import com.illunex.emsaasrestapi.license.mapper.LicensePartnershipMapper;
import com.illunex.emsaasrestapi.license.mapper.LicensePaymentHistoryMapper;
import com.illunex.emsaasrestapi.license.vo.LicensePartnershipVO;
import com.illunex.emsaasrestapi.license.vo.LicensePaymentHistoryVO;
import com.illunex.emsaasrestapi.license.vo.LicenseVO;
import com.illunex.emsaasrestapi.member.vo.MemberVO;
import com.illunex.emsaasrestapi.partnership.PartnershipComponent;
import com.illunex.emsaasrestapi.partnership.vo.PartnershipMemberVO;
import com.illunex.emsaasrestapi.payment.dto.PaymentPreviewResult;
import com.illunex.emsaasrestapi.payment.dto.RequestPaymentDTO;
import com.illunex.emsaasrestapi.payment.dto.ResponsePaymentDTO;
import com.illunex.emsaasrestapi.payment.mapper.*;
import com.illunex.emsaasrestapi.payment.util.*;
import com.illunex.emsaasrestapi.payment.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static com.illunex.emsaasrestapi.common.code.EnumCode.InvoiceItem.ItemTypeCd.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    private final InvoiceMapper invoiceMapper; // 재활용(필요시)
    private final LicensePartnershipMapper licensePartnershipMapper;
    private final LicenseMapper licenseMapper;
    private final PartnershipComponent partnershipComponent;
    private final PartnershipPaymentMethodMapper partnershipPaymentMethodMapper;
    private final TossPaymentService tossPaymentService;
    private final PaymentMandateMapper paymentMandateMapper;
    private final SubscriptionChangeEventMapper subscriptionChangeEventMapper;
    private final InvoiceItemMapper invoiceItemMapper;
    private final PaymentAttemptMapper paymentAttemptMapper;
    private final LicensePaymentHistoryMapper licensePaymentHistoryMapper;
    private final ProrationEngine prorationEngine;
    private final InvoicePaymentViewMapper invoicePaymentViewMapper;
    private final PaymentMethodViewMapper paymentMethodViewMapper;

    private final PaymentCleanupMapper paymentCleanupMapper;
    private final ProrationComponent prorationComponent;


    private final ModelMapper modelMapper;

    /**
     * Customer Key 생성
     * @return
     */
    public String createCustomerKey() {
        String customerKey = String.format(
                "SAAS-CK-%s-%s",
                LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE),
                UUID.randomUUID().toString().replace("-","").substring(0, 12).toUpperCase()
        );
        if (partnershipPaymentMethodMapper.selectByCustomerKey(customerKey).isPresent()) {
            return createCustomerKey();
        }
        return customerKey;
    }

    /**
     * 구독 변경 처리
     * @param req
     * @param member
     * @return
     * @throws CustomException
     */
    public ResponsePaymentDTO.LicenseChangeResult changeSubscription(RequestPaymentDTO.SubscriptionInfo req, MemberVO member) throws CustomException {
        // 0) 필수 파라미터 방어
        if (req == null || req.getPartnershipIdx() == null || req.getLicenseIdx() == null) {
            throw new CustomException(ErrorCode.COMMON_INVALID, "partnershipIdx/licenseIdx 누락");
        }

        // 1) 현재 구독 조회
        final Optional<LicensePartnershipVO> lpOpt = licensePartnershipMapper.selectByPartnershipIdx(req.getPartnershipIdx());

        // 2) 신규 구독: 즉시 과금
        if (lpOpt.isEmpty()) {
            return chargeNow(req, member);
        }

        // 3) 변경 의사결정
        final LicensePartnershipVO lp = lpOpt.get();

        final LicenseVO toLicense = licenseMapper.selectByIdx(req.getLicenseIdx())
                .orElseThrow(() -> new CustomException(ErrorCode.COMMON_INVALID, "대상 요금제 없음"));
        final LicenseVO fromLicense = licenseMapper.selectByIdx(lp.getLicenseIdx())
                .orElseThrow(() -> new CustomException(ErrorCode.COMMON_INVALID, "현재 요금제 없음"));

        final ChangeDecision decision = decideChange(fromLicense, toLicense);

        return switch (decision) {
            case UPGRADE ->
                // 업그레이드: 즉시 과금
                    chargeNow(req, member);
            case DOWNGRADE ->
                // 다운그레이드: 주기말 적용 예약
                    changeReservation(lp, EnumCode.LicensePartnership.StateCd.CHANGE.getCode(),
                            req.getLicenseIdx());
            case CANCEL_TO_FREE ->
                // 무료로 전환(=사실상 취소): 주기말 적용 예약
                    changeReservation(lp, EnumCode.LicensePartnership.StateCd.CANCEL.getCode(),
                            null);
            case NOOP -> throw new CustomException(ErrorCode.COMMON_INVALID, "동일 단가: 변경 불가");
            default -> throw new CustomException(ErrorCode.COMMON_INVALID, "알 수 없는 변경 결정");
        };
    }

    /**
     * 요금제 변경 의사결정
     */
    private ChangeDecision decideChange(LicenseVO fromLicense, LicenseVO toLicense) {
        final BigDecimal from = nullSafe(fromLicense.getPricePerUser());
        final BigDecimal to   = nullSafe(toLicense.getPricePerUser());

        if (to.signum() == 0) {
            return ChangeDecision.CANCEL_TO_FREE;
        }
        final int cmp = to.compareTo(from);
        if (cmp > 0) return ChangeDecision.UPGRADE;
        if (cmp < 0) return ChangeDecision.DOWNGRADE;
        return ChangeDecision.NOOP;
    }

    private enum ChangeDecision {
        NEW,           // 미사용(위에서 lpOpt.isEmpty()로 처리)
        UPGRADE,
        DOWNGRADE,
        CANCEL_TO_FREE,
        NOOP
    }

    private BigDecimal nullSafe(BigDecimal v) {
        return (v == null) ? BigDecimal.ZERO : v;
    }

    /**
     * 변경 예약: 파트너십 상태코드와 변경 이벤트를 기록하고, 결과 DTO 반환
     * - 좌석 증감이 아닌 “요금제 변경/취소”이므로 qtyDelta는 0 또는 null 권장
     */
    @Transactional
    public ResponsePaymentDTO.LicenseChangeResult changeReservation(LicensePartnershipVO lp, String stateCd, Integer toLicenseIdx) throws CustomException {
        ResponsePaymentDTO.LicenseChangeResult result = new ResponsePaymentDTO.LicenseChangeResult();
        // 1) 파트너십 상태 전환
        lp.setStateCd(stateCd);
        licensePartnershipMapper.updateByLicensePartnershipVO(lp);

        // 2) 이벤트 기록
        final SubscriptionChangeEventVO eventVO = new SubscriptionChangeEventVO();
        eventVO.setLicensePartnershipIdx(lp.getIdx());
        eventVO.setOccurredDate(ZonedDateTime.now());
        eventVO.setTypeCd(EnumCode.SubscriptionChangeEvent.TypeCd.PLAN_DOWNGRADE.getCode());
        eventVO.setFromLicenseIdx(lp.getLicenseIdx());
        eventVO.setToLicenseIdx(toLicenseIdx);
        eventVO.setQtyDelta(0);
        subscriptionChangeEventMapper.insertBySubscriptionChangeEventVO(eventVO);

        LicenseVO toLicense = toLicenseIdx != null ? licenseMapper.selectByIdx(toLicenseIdx)
                .orElse(null) : null;
        LicenseVO fromLicense = licenseMapper.selectByIdx(lp.getLicenseIdx())
                .orElse(null);

        // 3) 응답 구성
        result.setLicenseChangeStatueStatus(toLicense != null ?
                ResponsePaymentDTO.LicenseChangeResult.LicenseChangeStatus.DOWNGRADE :
                ResponsePaymentDTO.LicenseChangeResult.LicenseChangeStatus.CANCEL);
        result.setFromPlan(fromLicense != null ?
                modelMapper.map(fromLicense, ResponsePaymentDTO.PreviewPlan.class) :
                null);
        result.setToPlan(toLicense != null ?
                modelMapper.map(toLicense, ResponsePaymentDTO.PreviewPlan.class) :
                null);
        return result;
    }

    private String buildReservationMessage(String stateCd, LicensePartnershipVO lp) {
        if (EnumCode.LicensePartnership.StateCd.CANCEL.getCode().equals(stateCd)) {
            return "구독 취소가 주기 종료 시점에 적용됩니다. partnershipIdx=" + lp.getPartnershipIdx();
        }
        if (EnumCode.LicensePartnership.StateCd.CHANGE.getCode().equals(stateCd)) {
            return "요금제 변경이 주기 종료 시점에 적용됩니다. partnershipIdx=" + lp.getPartnershipIdx();
        }
        return "변경 예약이 적용됩니다. partnershipIdx=" + lp.getPartnershipIdx();
    }

    /**
     * 즉시 결제
     */
    public ResponsePaymentDTO.LicenseChangeResult chargeNow(RequestPaymentDTO.SubscriptionInfo req, MemberVO member) throws CustomException {
        final PaymentPreviewResult preview = paymentPreviewResultForCharge(req, member); // 결제수단 검증 목적

        // A) TX-1: 인보이스 준비 + 시도기록(PENDING) 생성
        Tx1Context tx1 = tx1_prepareInvoiceAndBeginAttemptByPaymentPreviewResult(req, preview);

        // B) PG 호출 (비트랜잭션)
        JSONObject pgResp = callPgConfirmBillingByPreview(tx1, preview);

        // C) TX-2: 응답 반영(시도 성공/실패 + 수납 + 인보이스 상태 전이 + 구독 스냅샷 적용)

        return tx2_finalizeAttemptAndReceiptByPreview(preview, tx1, pgResp);
    }

    public PaymentPreviewResult paymentPreviewResultForCharge(RequestPaymentDTO.SubscriptionInfo req, MemberVO member) throws CustomException {
        // 결제수단 정보 조회
        PartnershipPaymentMethodVO defaultMethod = partnershipPaymentMethodMapper.selectDefaultByPartnershipIdx(req.getPartnershipIdx())
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NO_DEFAULT_METHOD));
        PaymentMandateVO defaultMandate = paymentMandateMapper.selectLastOneByPaymentMethodIdx(defaultMethod.getIdx())
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NO_DEFAULT_METHOD));
        // 1) 프리뷰 입력 생성
        final ProrationInput input = prorationComponent.buildInputForPreview(req, member);

        // 2) 프리뷰 금액 산출 (NEW_TO_PAID 분기 포함)
        final PaymentPreviewResult preview = paymentPreviewResult(input); // 이전에 다듬은 메서드 사용

        String orderNumber = (req.getOrderNumber() != null && !req.getOrderNumber().isBlank())
                ? req.getOrderNumber()
                : createTossOrderId();
        preview.setOrderId(orderNumber);
        preview.setCustomerKey(defaultMethod.getCustomerKey());
        preview.setBillingKey(defaultMandate.getMandateId());
        preview.setCustomerName(member.getName());
        preview.setCustomerEmail(member.getEmail());
        preview.setOrderName(input.getToPlan().getName() + " 외 " + (preview.getItems().size() -1) + "건");
        preview.setPartnershipIdx(req.getPartnershipIdx());
        preview.setFromLicenseIdx(input.getFromPlan() != null ? input.getFromPlan().getIdx() : null);
        preview.setToLicenseIdx(input.getToPlan().getPlanCd().equals(EnumCode.License.PlanCd.BASIC.getCode()) ? null : input.getToPlan().getIdx());
        return preview;
    }

    public Object getPaymentMethodToss(Integer partnershipIdx, MemberVO memberVO) throws CustomException {
        PartnershipMemberVO partnershipMember = partnershipComponent.checkPartnershipMember(memberVO, partnershipIdx);

        List<PaymentMethodViewVO> methodList = paymentMethodViewMapper.selectPaymentMethodsByPartnershipIdx(partnershipIdx);
        return methodList;
    }

    // ===================== TX-1 =====================

    @Transactional
    protected Tx1Context tx1_prepareInvoiceAndBeginAttemptByPaymentPreviewResult(RequestPaymentDTO.SubscriptionInfo req, PaymentPreviewResult preview) throws CustomException {
        // (A) LP 보장: 없으면 DRAFT로 생성
        LicensePartnershipVO lp = ensureLicensePartnershipForNewIfAbsent(req, preview);

        // 1) 멱등 – orderNumber 중복 체크
        PaymentAttemptVO dup = paymentAttemptMapper.selectByOrderNumber(preview.getOrderId());
        if (dup != null) {
            log.info("[tx1] duplicate orderNumber exists. attemptIdx={}", dup.getIdx());
            InvoiceVO inv = invoiceMapper.selectByIdx(dup.getInvoiceIdx());
            return Tx1Context.fromExistingAttempt(dup, inv, readDefaultMethodMandate(req.getPartnershipIdx()), lp);
        }

        InvoiceVO invoice = upsertDraftOpenInvoice(lp.getIdx(), preview);

        // 3) 기본 결제수단 + 활성 Mandate 조회
        DefaultMethodMandate mm = readDefaultMethodMandate(preview.getPartnershipIdx());
        if (mm == null || mm.paymentMethodIdx() == null || mm.paymentMandateIdx() == null || mm.providerCd() == null) {
            throw new CustomException(ErrorCode.PAYMENT_NO_DEFAULT_METHOD);
        }

        // 4) 시도기록(PENDING)
        PaymentAttemptVO attempt = new PaymentAttemptVO();
        attempt.setInvoiceIdx(invoice.getIdx());
        attempt.setPartnershipIdx(preview.getPartnershipIdx());
        attempt.setProviderCd(mm.providerCd());
        attempt.setPaymentMethodIdx(mm.paymentMethodIdx());
        attempt.setPaymentMandateIdx(mm.paymentMandateIdx());
        attempt.setAttemptNo(resolveNextAttemptNo(invoice.getIdx()));
        attempt.setAmount(BigDecimal.valueOf(preview.getAmount()));
        attempt.setUnitCd("MUC0001");
        attempt.setStatusCd(EnumCode.PaymentAttempt.StateCd.PENDING.getCode());
        attempt.setOrderNumber(preview.getOrderId());
//        attempt.setMeta(buildAttemptMeta(input, result));
        paymentAttemptMapper.insertByPaymentAttemptVO(attempt);

        return new Tx1Context(invoice, attempt, mm, lp);
    }

    private LicensePartnershipVO ensureLicensePartnershipForNewIfAbsent(RequestPaymentDTO.SubscriptionInfo req, PaymentPreviewResult preview) throws CustomException {
        var lpOpt = licensePartnershipMapper.selectByPartnershipIdx(preview.getPartnershipIdx());
        if (lpOpt.isPresent()) return lpOpt.get();
        var license = licenseMapper.selectByIdx(req.getLicenseIdx())
                .orElseThrow(() -> new CustomException(ErrorCode.COMMON_INVALID));

        LicensePartnershipVO lp = new LicensePartnershipVO();
        lp.setPartnershipIdx(preview.getPartnershipIdx());
        lp.setLicenseIdx(req.getLicenseIdx());
        lp.setBillingDay(preview.getPeriodEnd().getDayOfMonth());
        lp.setPeriodStartDate(preview.getPeriodStart());
        lp.setPeriodEndDate(preview.getPeriodEnd());
        lp.setNextBillingDate(preview.getPeriodEnd()); // 보통 = period_end_excl
        lp.setCurrentSeatCount(partnershipComponent.getPartnershipActiveMemberCount(req.getPartnershipIdx()));
        lp.setCurrentUnitPrice(license.getPricePerUser());
        lp.setCurrentMinUserCount(license.getMinUserCount());
        lp.setCancelAtPeriodEnd(false);
        lp.setStateCd(EnumCode.LicensePartnership.StateCd.DRAFT.getCode()); // DRAFT
        licensePartnershipMapper.insertByLicensePartnership(lp);  // useGeneratedKeys=true
        return lp;
    }

    /**
     * 인보이스 멱등 생성(DRAFT) → 아이템 채우기 → 합계 재계산 → OPEN
     */
    @Transactional
    protected InvoiceVO upsertDraftOpenInvoice(Integer lpIdx, PaymentPreviewResult preview) {
        var inv = createDraftInvoice(lpIdx, preview);

        // 아이템 보장(엔진 결과 기반으로 PRORATION/RECURRING/CREDIT 채우기)
        ensureInvoiceItems(inv, preview);

        // 합계/세금/총액 재계산
        invoiceMapper.recalcTotals(inv.getIdx(), BigDecimal.ZERO);

        // DRAFT → OPEN 1회성 전환(이미 OPEN이면 영향 없음)
        invoiceMapper.markOpen(inv.getIdx());

        return invoiceMapper.selectByIdx(inv.getIdx());
    }

    @Transactional
    protected InvoiceVO createDraftInvoice(Integer licensePartnershipIdx, PaymentPreviewResult preview) {
        InvoiceVO inv = new InvoiceVO();
        inv.setPartnershipIdx(preview.getPartnershipIdx());
        inv.setLicensePartnershipIdx(licensePartnershipIdx);
        inv.setPeriodStart(preview.getPeriodStart());
        inv.setPeriodEnd(preview.getPeriodEnd());
        inv.setSubtotal(BigDecimal.ZERO);
        inv.setTax(BigDecimal.ZERO);
        inv.setTotal(BigDecimal.ZERO);
        inv.setStatusCd(EnumCode.Invoice.StateCd.DRAFT.getCode());
        inv.setLicenseIdx(preview.getToLicenseIdx() != null ? preview.getToLicenseIdx() : null);
        inv.setUnitCd("MUC0001");
        invoiceMapper.insertByInvoiceVO(inv);
        return inv;
    }

    /**
     * 인보이스 아이템 채움 – 엔진 결과를 invoice_item으로 적재
     * (동일 항목이 이미 존재하면 중복 삽입하지 않도록 키/메타 기준으로 방어 로직을 추가할 수 있음)
     */
    @Transactional
    protected void ensureInvoiceItems(InvoiceVO inv, PaymentPreviewResult preview) {
        // 예시: result.getLines()가 청구 항목 리스트라고 가정
        if (preview.getItems() == null) return;

        for (PaymentPreviewResult.PreviewResultItem resultItem : preview.getItems()) {
            InvoiceItemVO item = new InvoiceItemVO();
            item.setInvoiceIdx(inv.getIdx());
            item.setItemTypeCd(mapItemType(resultItem.getItemType()));
            item.setDescription(resultItem.getDescription());
            item.setQuantity(resultItem.getQuantity());
            item.setUnitPrice(BigDecimal.valueOf(resultItem.getUnitPrice()));
            item.setDays(resultItem.getDays());
            item.setAmount(BigDecimal.valueOf(resultItem.getAmount()));
//            item.setMeta(buildItemMeta(input, prorationItem));
            invoiceItemMapper.insertByInvoiceItemVO(item);
        }
    }

    // ===================== PG 호출 =====================

    /**
     * 비트랜잭션: 토스 정기결제 confirm 호출
     */
    protected JSONObject callPgConfirmBillingByPreview(Tx1Context tx1, PaymentPreviewResult preview) throws CustomException {
        try {
            TossConfirmPayload tossConfirmPayload = TossConfirmPayload.of(
                    tx1.mandate().providerCd(),
                    tx1.mandate().paymentMethodIdx(),
                    tx1.mandate().paymentMandateIdx(),
                    tx1.attempt().getOrderNumber(),
                    BigDecimal.valueOf(preview.getAmount())
            );
            JSONObject payload = new JSONObject(tossConfirmPayload);
            payload.put("billingKey", tx1.mandate.mandateId());
            payload.put("orderName", preview.getOrderName());
            payload.put("orderId", tx1.attempt().getOrderNumber());
            payload.put("amount", tossConfirmPayload.amount());
            payload.put("customerKey", tx1.mandate.customerKey());
            payload.put("customerEmail", preview.getCustomerEmail());
            payload.put("customerName", preview.getCustomerName());
            return tossPaymentService.confirmBilling(payload);
        } catch (IOException ioe) {
            log.error("PG confirmBilling network error", ioe);
            // TX-2에서 실패 반영을 위해 예외로 넘김
            throw new CustomException(ErrorCode.PG_TOSS_PAYMENT_FAIL);
        }
    }

    // ===================== TX-2 =====================

    @Transactional
    protected ResponsePaymentDTO.LicenseChangeResult tx2_finalizeAttemptAndReceiptByPreview(PaymentPreviewResult preview,
                                                                                            Tx1Context tx1,
                                                                                            JSONObject pgResp) throws CustomException {
        ResponsePaymentDTO.LicenseChangeResult changeResult = new ResponsePaymentDTO.LicenseChangeResult();
        PaymentAttemptVO attempt = tx1.attempt();
        ZonedDateTime now = ZonedDateTime.now();
        InvoiceVO invoice = tx1.invoice();

        // 1) 응답 검증 (금액/통화/성공여부)
        PgVerification vr = verifyPgResponse(pgResp, attempt.getOrderNumber(), BigDecimal.valueOf(preview.getAmount()));

        // 2) 시도결과 업데이트
        attempt.setRespondDate(now);
        attempt.setMeta(pgResp == null ? null : pgResp.toString());

        if (!vr.success) {
            attempt.setStatusCd(EnumCode.PaymentAttempt.StateCd.FAILED.getCode());
            attempt.setFailureCode(vr.code);
            attempt.setFailureMessage(vr.message);
            paymentAttemptMapper.updateByPaymentAttemptVO(attempt);
            // 인보이스는 OPEN 유지 – 재시도 가능
            changeResult.setErrorCode(vr.code);
            changeResult.setErrorMessage(vr.message);
            return changeResult;
        }

        attempt.setStatusCd(EnumCode.PaymentAttempt.StateCd.SUCCESS.getCode());
        paymentAttemptMapper.updateByPaymentAttemptVO(attempt);

        // 3) 수납 기록 (중복방지: (provider_cd, order_number) UNIQUE 권장)
        LicensePaymentHistoryVO pay = new LicensePaymentHistoryVO();
        pay.setInvoiceIdx(tx1.invoice().getIdx());
        pay.setProviderCd(tx1.mandate().providerCd());
        pay.setOrderNumber(attempt.getOrderNumber());

        pay.setAmount(BigDecimal.valueOf(preview.getAmount()));
        pay.setUnitCd("MUC0001");
        pay.setMeta(pgResp == null ? null : pgResp.toString());
        pay.setPaidDate(now);
        licensePaymentHistoryMapper.insertByLicensePaymentHistoryVO(pay);

        // 4) 미납 계산 → 0원이면 PAID
        InvoicePaymentView summary = invoicePaymentViewMapper.selectInvoicePaymentSummary(tx1.invoice().getIdx());
        if (summary != null && summary.getBalanceDue() != null
                && summary.getBalanceDue().compareTo(BigDecimal.ZERO) <= 0) {
            String receiptUrl = Optional.ofNullable(pgResp.optJSONObject("receipt"))
                    .map(r -> r.optString("url", null))
                    .orElse(null);
            invoice.setChargeUserCount(partnershipComponent.getPartnershipActiveMemberCount(tx1.lp.getPartnershipIdx()));
            invoice.setReceiptUrl(receiptUrl);
            invoice.setStatusCd(EnumCode.Invoice.StateCd.PAID.getCode());
            invoiceMapper.updateByInvoiceVO(invoice);
        }

        // 5) 구독정보 변경
        applyChangeLicensePartnership(tx1.lp, preview);
        Optional<LicenseVO> fromLicense = preview.getFromLicenseIdx() != null ? licenseMapper.selectByIdx(preview.getFromLicenseIdx()) : Optional.empty();
        Optional<LicenseVO> toLicense = licenseMapper.selectByIdx(preview.getToLicenseIdx());
        ResponsePaymentDTO.PreviewPlan fromPlan = fromLicense.map(l -> modelMapper.map(l, ResponsePaymentDTO.PreviewPlan.class)).orElse(null);
        ResponsePaymentDTO.PreviewPlan toPlan = toLicense.map(l -> modelMapper.map(l, ResponsePaymentDTO.PreviewPlan.class)).orElse(null);

        // 6) 구독 변경 이벤트 생성
        SubscriptionChangeEventVO event = new SubscriptionChangeEventVO();
        event.setLicensePartnershipIdx(tx1.lp.getIdx());
        event.setOccurredDate(LocalDate.now().atStartOfDay(ZoneId.systemDefault()));
        event.setTypeCd(EnumCode.SubscriptionChangeEvent.TypeCd.PLAN_UPGRADE.getCode());
        event.setFromLicenseIdx(preview.getFromLicenseIdx());
        event.setToLicenseIdx(preview.getToLicenseIdx());
        event.setQtyDelta(partnershipComponent.getPartnershipActiveMemberCount(tx1.lp.getPartnershipIdx())); // 전체 좌석 수로 이벤트 생성

        subscriptionChangeEventMapper.insertBySubscriptionChangeEventVO(event);

        changeResult.setAmount(tx1.invoice().getTotal().intValue());
        changeResult.setOrderId(attempt.getOrderNumber());
        changeResult.setOrderName(preview.getOrderName());
        changeResult.setToPlan(toPlan);
        changeResult.setFromPlan(fromPlan);
        changeResult.setLicenseChangeStatueStatus(ResponsePaymentDTO.LicenseChangeResult.LicenseChangeStatus.UPGRADE);

        return changeResult;
    }

    @Transactional
    protected void applyChangeLicensePartnership(LicensePartnershipVO lp, PaymentPreviewResult preview) throws CustomException {
        // 예시: license_partnership의 period_start/end/next_billing_date, current_* 스냅샷 갱신
        // - input.getNextPeriodStart(), input.getNextPeriodEndExcl()
        // - input.getChargeSeats(), input.getUnitPriceSnapshot(), input.getMinUserCountSnapshot()
        lp.setStateCd(EnumCode.LicensePartnership.StateCd.ACTIVE.getCode());
        lp.setLicenseIdx(preview.getToLicenseIdx());
        lp.setCurrentSeatCount(partnershipComponent.getPartnershipActiveMemberCount(lp.getPartnershipIdx()));
        licensePartnershipMapper.updateByLicensePartnershipVO(lp);
    }

    // ===================== 유틸리티/검증/매핑 =====================

    protected int resolveNextAttemptNo(Integer invoiceIdx) {
        // 간단 구현: 시도번호를 현재 timestamp 기반으로 1로 시작(권장: SELECT MAX(attempt_no)+1)
        // 필요 시 전용 Mapper 메서드를 만들어 MAX+1 가져오세요.
        return 1;
    }

    /**
     * 기본 결제수단 + 활성 Mandate 조회 (invoicePaymentViewMapper에서 map 반환)
     */
    protected DefaultMethodMandate readDefaultMethodMandate(Integer partnershipIdx) {
        Map<String, Object> m = invoicePaymentViewMapper.selectDefaultMethodAndMandate(partnershipIdx);
        if (m == null) return null;
        Integer methodIdx  = (Integer) m.get("paymentMethodIdx");
        Integer mandateIdx = (Integer) m.get("paymentMandateIdx");
        String  providerCd = (String)  m.get("providerCd");
        String mandateId  = (String)  m.get("mandateId");
        String customerKey = (String) m.get("customerKey");
        return new DefaultMethodMandate(methodIdx, mandateIdx, providerCd, mandateId, customerKey);
    }

    /**
     * PG 응답 검증 (성공여부/금액 일치 등)
     * 실제 Toss 응답 스키마에 맞춰 amount, currency, status 등을 검증하도록 구현
     */
    protected PgVerification verifyPgResponse(JSONObject resp, String orderNumber, BigDecimal expectedTotal) {
        if (resp == null) return PgVerification.fail("PG_NULL", "PG 응답이 없습니다.");
        try {
            String status = resp.optString("status"); // 예: "DONE"
            BigDecimal paid = new BigDecimal(resp.optString("totalAmount", "0"));
            boolean ok = "DONE".equalsIgnoreCase(status) && paid.compareTo(expectedTotal) == 0;

            if (!ok) {
                return PgVerification.fail("PG_MISMATCH", "PG 상태/금액 불일치");
            }
            return PgVerification.ok();
        } catch (Exception e) {
            log.error("verifyPgResponse parse error", e);
            return PgVerification.fail("PG_PARSE", "PG 응답 파싱 실패");
        }
    }

    @Transactional
    public void testCleanup(Integer partnershipIdx) {
        paymentCleanupMapper.deleteLicensePaymentHistoryByPartnership(partnershipIdx);
        paymentCleanupMapper.deleteInvoiceItemsByPartnership(partnershipIdx);
        paymentCleanupMapper.deletePaymentAttemptsByPartnership(partnershipIdx);
        paymentCleanupMapper.deleteInvoicesByPartnership(partnershipIdx);
        paymentCleanupMapper.deleteSubscriptionEventsByPartnership(partnershipIdx);
        paymentCleanupMapper.deleteLicensePartnershipsByPartnership(partnershipIdx);
        paymentCleanupMapper.deletePaymentMandatesByPartnership(partnershipIdx);
        paymentCleanupMapper.deletePaymentMethodsByPartnership(partnershipIdx);
    }

    // ===================== DTO/VO 보조 구조체 =====================

    /**
     * TX-1 결과 컨텍스트
     */
    protected record Tx1Context(InvoiceVO invoice, PaymentAttemptVO attempt, DefaultMethodMandate mandate, LicensePartnershipVO lp) {
        static Tx1Context fromExistingAttempt(PaymentAttemptVO attempt, InvoiceVO invoice, DefaultMethodMandate mm, LicensePartnershipVO lp) {
            return new Tx1Context(invoice, attempt, mm, lp);
        }
    }

    /**
     * 기본 결제수단 + Mandate
     */
    protected record DefaultMethodMandate(Integer paymentMethodIdx, Integer paymentMandateIdx, String providerCd, String mandateId, String customerKey) {}

    /**
     * PG 응답 검증 결과
     */
    protected static class PgVerification {
        final boolean success;
        final String code;
        final String message;

        private PgVerification(boolean success, String code, String message) {
            this.success = success;
            this.code = code;
            this.message = message;
        }

        static PgVerification ok() { return new PgVerification(true, null, null); }
        static PgVerification fail(String code, String message) { return new PgVerification(false, code, message); }

        public boolean isSuccess() { return success; }
        public String code() { return code; }
        public String message() { return message; }
    }

    /**
     * 토스 Confirm 페이로드 예시 (프로젝트 실 시그니처에 맞게 조정)
     */
    protected record TossConfirmPayload(String providerCd, Integer paymentMethodIdx, Integer paymentMandateIdx,
                                        String orderNumber, BigDecimal amount) {
        static TossConfirmPayload of(String providerCd, Integer pmIdx, Integer mdIdx, String orderNumber, BigDecimal amount) {
            return new TossConfirmPayload(providerCd, pmIdx, mdIdx, orderNumber, amount);
        }
    }

    // ===================== 빌드/계산 입력 (프로젝트 내 기존 메서드 사용 가정) =====================

    /**
     * 결제수단 등록
     * @param request
     * @param memberVO
     * @throws CustomException
     */
    @Transactional
    public ResponsePaymentDTO.RegisterPaymentMethod registerPaymentMethodToss(RequestPaymentDTO.MethodRegister request, MemberVO memberVO) throws CustomException, IOException {
        PartnershipMemberVO partnershipMember = partnershipComponent.checkPartnershipMember(memberVO, request.getPartnershipIdx());
        // TODO 결제 권한 보유여부 체크

        JSONObject resp = tossPaymentService.issueBillingKey(request.getCustomerKey(), request.getAuthKey());
        JSONObject card = resp.getJSONObject("card");

        partnershipPaymentMethodMapper.selectDefaultByPartnershipIdx(request.getPartnershipIdx()).ifPresent(existingDefault -> {
            existingDefault.setIsDefault(false);
            partnershipPaymentMethodMapper.updateByPartnershipPaymentMethodVO(existingDefault);
        });

        PartnershipPaymentMethodVO methodVO = new PartnershipPaymentMethodVO();
        methodVO.setPartnershipIdx(request.getPartnershipIdx());
        methodVO.setAuthKey(request.getAuthKey());
        methodVO.setCustomerKey(request.getCustomerKey());
        methodVO.setMethodTypeCd(EnumCode.PaymentMethod.MethodTypeCd.CARD.getCode()); // 카드
        methodVO.setStateCd(EnumCode.PaymentMethod.StateCd.ACTIVE.getCode()); // 활성
        methodVO.setIsDefault(true);
        methodVO.setLast4(request.getLast4());
        methodVO.setBrand(resp.getString("cardCompany"));
        partnershipPaymentMethodMapper.insertByPartnershipPaymentMethodVO(methodVO);

        PaymentMandateVO mandateVO = new PaymentMandateVO();
        mandateVO.setPartnershipIdx(request.getPartnershipIdx());
        mandateVO.setPaymentMethodIdx(methodVO.getIdx());
        mandateVO.setProviderCd(EnumCode.PaymentMandate.ProviderCd.TOSS.getCode());
        mandateVO.setMandateId(resp.getString("billingKey"));
        mandateVO.setStatusCd(EnumCode.PaymentMandate.StatusCd.ACTIVE.getCode()); // 활성
        paymentMandateMapper.insertByPaymentMandateVO(mandateVO);

        ResponsePaymentDTO.RegisterPaymentMethod result = new ResponsePaymentDTO.RegisterPaymentMethod();
        result.setMethod(methodVO.getMethodTypeCd());
        result.setCard(card.toMap());
        result.setPaymentMethodIdx(methodVO.getIdx());
        return result;
    }

    /**
     * 정산 금액 계산 (프리뷰)
     * @param req
     * @param memberVO
     * @return
     * @throws CustomException
     */
    @Transactional(readOnly = true)
    public ResponsePaymentDTO.PaymentPreview calculateProrationAmount(RequestPaymentDTO.SubscriptionInfo req,
                                                                      MemberVO memberVO) throws CustomException {
        // 1) 프리뷰 입력 생성
        final ProrationInput input = prorationComponent.buildInputForPreview(req, memberVO);

        if (input.getCaseType() == ProrationInput.CaseType.NEW_TO_PAID || input.getCaseType() == ProrationInput.CaseType.UPGRADE) {
            // 2) 프리뷰 금액 산출 (NEW_TO_PAID 분기 포함)
            final PaymentPreviewResult preview = paymentPreviewResult(input); // 이전에 다듬은 메서드 사용

            // 3) DTO 매핑 + 세금/총액/크레딧 처리
            ResponsePaymentDTO.PaymentPreview resp = buildPreviewResponse(input, preview);
            resp.setPartnershipIdx(req.getPartnershipIdx());
            return buildPreviewResponse(input, preview);
        }

        return null;

    }

    private ResponsePaymentDTO.PaymentPreview buildPreviewResponse(ProrationInput input,
                                                                   PaymentPreviewResult preview) {
        final List<ResponsePaymentDTO.PreviewItem> items =
                (preview.getItems() == null ? List.<PaymentPreviewResult.PreviewResultItem>of() : preview.getItems())
                        .stream()
                        .map(this::mapPreviewItem)
                        .collect(Collectors.toList());

        final long subTotal = items.stream()
                .map(ResponsePaymentDTO.PreviewItem::getAmount)
                .filter(Objects::nonNull)
                .mapToLong(Long::longValue)
                .sum();

        long creditCarryOver = 0L;

        final LocalDate start = preview.getPeriodStart();
        final LocalDate endExcl = preview.getPeriodEnd();

        final ResponsePaymentDTO.PaymentPreview out = new ResponsePaymentDTO.PaymentPreview();
        out.setLicensePartnershipIdx(null); // 필요 시 prorationComponent에서 노출받아 세팅
        out.setCurrentPlan(null);           // PreviewPlan 매핑 필요 시 별도 mapPlan(...) 구현
        out.setTargetPlan(null);

        // 청구 좌석: 아이템에 좌석이 있는 경우 우선 사용, 없으면 입력 스냅샷/활성 기준
        out.setPeriodStart(start);
        out.setPeriodEnd(endExcl);
        out.setDenominatorDays(start != null && endExcl != null
                ? (int) ChronoUnit.DAYS.between(start, endExcl)
                : null);

        out.setOccurredAt(input.getPaymentTime().toOffsetDateTime().toString());
        out.setRoundingRule(input.getRoundingMode() == null ? "HALF_UP" : input.getRoundingMode().toString());
        out.setCurrency(input.getCurrency() == null ? "KRW" : input.getCurrency());
        out.setItems(items);

        out.setSubTotal(subTotal);
        out.setWillChargeNow(true); // 선결제/즉시결제 정책. 필요 시 케이스별로 분기
        out.setCreditCarryOver(creditCarryOver);

        out.setNotes(buildNotes(input, out));
        return out;
    }

    private ResponsePaymentDTO.PreviewItem mapPreviewItem(PaymentPreviewResult.PreviewResultItem src) {
        final ResponsePaymentDTO.PreviewItem t = new ResponsePaymentDTO.PreviewItem();
        t.setItemType(src.getItemType());
        t.setDescription(src.getDescription());
        t.setQuantity(src.getQuantity());
        t.setUnitPrice(src.getUnitPrice());
        t.setDays(src.getDays());
        t.setAmount(src.getAmount());
        t.setRelatedEventId(src.getRelatedEventId());
        t.setMeta(src.getMeta());
        return t;
    }

    private List<String> buildNotes(ProrationInput input, ResponsePaymentDTO.PaymentPreview out) {
        return List.of(
                "case=" + input.getCaseType(),
                "denominatorDays=" + out.getDenominatorDays(),
                "rounding=" + out.getRoundingRule()
        );
    }


    public PaymentPreviewResult paymentPreviewResult(ProrationInput input) throws CustomException {
        if (input == null || input.getToPlan() == null || input.getCaseType() == null) {
            throw new CustomException(ErrorCode.COMMON_INVALID);
        }

        // NEW_TO_PAID: 정기 과금 1개월 선청구(기간: input.periodStart ~ input.periodEndExcl)
        if (input.getCaseType() == ProrationInput.CaseType.NEW_TO_PAID) {
            // 좌석 기준: snapshot → active → 0
            final int snapshot = input.getSnapshotSeats() != null ? input.getSnapshotSeats() : -1;
            final int active   = input.getActiveSeats();
            final int currentSeats = (snapshot >= 0 ? snapshot : (active >= 0 ? active : 0));

            final int minUsers   = nz(input.getToPlan().getMinUserCount());
            final int chargeSeat = Math.max(currentSeats, minUsers);

            // 단가/금액 계산(Long 정수 KRW)
            final long unitPrice = input.getToPlan().getPricePerUser().longValueExact(); // BigDecimal → long(정수 KRW 가정)
            final long lineAmount = Math.multiplyExact(chargeSeat, unitPrice);

            PaymentPreviewResult.PreviewResultItem item =
                    PaymentPreviewResult.PreviewResultItem.builder()
                            .itemType("RECURRING")
                            .description("New subscription charge")
                            .quantity(chargeSeat)
                            .unitPrice(unitPrice)
                            .days(null)              // 월 선청구: 일수 개념 없음
                            .amount(lineAmount)
                            .build();

            return PaymentPreviewResult.builder()
                    .items(List.of(item))                           // of()는 불변으로 감싸지만 여기선 그대로 둠
                    .periodStart(input.getPeriodStart())
                    .periodEnd(input.getPeriodEndExcl())
                    .amount(Math.toIntExact(lineAmount))            // 총액(Integer) — 정책상 int 사용
                    .build();
        }

        // 그 외(업/다운그레이드, 동일주기 내 좌석변경 등): 프레이션 엔진 결과 사용
        final ProrationResult prorationResult = prorationEngine.calculate2(input);
        return PaymentPreviewResult.of(prorationResult);
    }

    // null→0 보정
    private static int nz(Integer v) { return v == null ? 0 : v; }

    private String mapItemType(String itemType) {
        return switch (itemType) {
            case "RECURRING" -> RECURRING.getCode(); // "ITC0001"
            case "PRORATION"  -> PRORATION.getCode();
            case "FIX" -> FIX.getCode();
            case "ADJUST" -> ADJUST.getCode();
            default -> throw new IllegalArgumentException("unknown item type: " + itemType);
        };
    }

    private String createTossOrderId() {
        String orderId = String.format(
                "SAAS-OD-%s-%s",
                LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE),
                UUID.randomUUID().toString().replace("-","").substring(0, 12).toUpperCase()
        );
        if (paymentAttemptMapper.selectByOrderNumber(orderId) != null) {
            return createTossOrderId();
        }
        return orderId;
    }
}
