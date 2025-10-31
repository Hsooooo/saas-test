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
import com.illunex.emsaasrestapi.partnership.mapper.PartnershipMapper;
import com.illunex.emsaasrestapi.partnership.vo.PartnershipMemberVO;
import com.illunex.emsaasrestapi.payment.dto.PaymentCommand;
import com.illunex.emsaasrestapi.payment.dto.RequestPaymentDTO;
import com.illunex.emsaasrestapi.payment.dto.ResponsePaymentDTO;
import com.illunex.emsaasrestapi.payment.mapper.*;
import com.illunex.emsaasrestapi.payment.util.BillingAnchorPolicy;
import com.illunex.emsaasrestapi.payment.util.ProrationEngine;
import com.illunex.emsaasrestapi.payment.util.ProrationInput;
import com.illunex.emsaasrestapi.payment.util.ProrationResult;
import com.illunex.emsaasrestapi.payment.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.modelmapper.ModelMapper;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.illunex.emsaasrestapi.common.code.EnumCode.InvoiceItem.ItemTypeCd.*;
import static java.time.temporal.ChronoUnit.DAYS;

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
     * 즉시 결제(오늘 결제, 내일부터 적용) – 오케스트레이션 메서드
     */
    public ResponsePaymentDTO.PaymentChargeResult chargeNow(RequestPaymentDTO.SubscriptionChangeEvent req, MemberVO member) throws CustomException {
        // 1) 구독 존재여부 확인
        var lpOpt = licensePartnershipMapper.selectByPartnershipIdx(req.getPartnershipIdx());

        ProrationInput input;
        if (lpOpt.isEmpty()) {
            // ▶ 신규 구독용 입력
            input = buildInputForNewSubscription_Tomorrow(req, member);
        } else {
            // ▶ 기존 구독용 입력 (지금 쓰던 buildInputForCharge_Tomorrow)
            input = buildInputForCharge_Tomorrow(req, member);
        }


        // 0) 계산 입력(BUILD) – 정책: "오늘 결제, 내일부터 적용"
        ProrationResult result = prorationEngine.calculate(input);

        if (result == null) {
            return ResponsePaymentDTO.PaymentChargeResult.zero();
        }

        // 멱등 키 – 프론트 전달값을 우선 사용, 없으면 서버에서 생성
        String orderNumber = (req.getOrderNumber() != null && !req.getOrderNumber().isBlank())
                ? req.getOrderNumber()
                : createTossOrderId();

        // A) TX-1: 인보이스 준비 + 시도기록(PENDING) 생성
        Tx1Context tx1 = tx1_prepareInvoiceAndBeginAttempt(input, result, orderNumber);

        // B) PG 호출 (비트랜잭션)
        JSONObject pgResp = callPgConfirmBilling(tx1, result);

        // C) TX-2: 응답 반영(시도 성공/실패 + 수납 + 인보이스 상태 전이 + 구독 스냅샷 적용)
        return tx2_finalizeAttemptAndReceipt(input, result, tx1, pgResp);
    }

    public Object getPaymentMethodToss(Integer partnershipIdx, MemberVO memberVO) throws CustomException {
        PartnershipMemberVO partnershipMember = partnershipComponent.checkPartnershipMember(memberVO, partnershipIdx);

        List<PaymentMethodViewVO> methodList = paymentMethodViewMapper.selectPaymentMethodsByPartnershipIdx(partnershipIdx);
        return methodList;
    }

    protected ProrationInput buildInputForNewSubscription_Tomorrow(RequestPaymentDTO.SubscriptionChangeEvent req, MemberVO member) throws CustomException {
        var license = licenseMapper.selectByIdx(req.getLicenseIdx())
                .orElseThrow(() -> new CustomException(ErrorCode.COMMON_INVALID));
        int currentSeats = partnershipComponent.getPartnershipActiveMemberCount(req.getPartnershipIdx());

        LocalDate today = LocalDate.now();
        LocalDate start  = today.plusDays(1);
        int anchor = resolveBillingDayAnchor(start, 28); // 보통 start.dayOfMonth 또는 정책상 고정 anchor
        PeriodRange next = resolvePeriodFromAnchor(start, anchor); // [start, endExcl)

        int requestedSeats = Math.max(currentSeats, 1);
        int minSeats = Math.max(license.getMinUserCount(), 1);
        int chargeSeats = Math.max(requestedSeats, minSeats);

        return ProrationInput.builder()
                .partnershipIdx(req.getPartnershipIdx())
                .licenseIdx(req.getLicenseIdx())
                // 구 주기(없음) 대신 “표시/보정”을 위해 nextPeriod를 period로 사용
                .periodStart(next.start())
                .periodEndExcl(next.endExcl())
                .today(today)
                .denominatorDays((int) DAYS.between(next.start(), next.endExcl()))
                .denominatorDaysNew((int) DAYS.between(next.start(), next.endExcl()))
                .roundingMode(RoundingMode.HALF_UP)
                .currency("KRW")
                .fromPlan(null) // 구 플랜 없음
                .toPlan(ProrationInput.Plan.builder()
                        .idx(license.getIdx()).planCd(license.getPlanCd())
                        .name(license.getName())
                        .pricePerUser(license.getPricePerUser())
                        .minUserCount(license.getMinUserCount()).build())
                .prepaidSeats(0) // 신규는 선불 좌석 없음
                .minChargeSeats(license.getMinUserCount() == null ? 0 : license.getMinUserCount())
                .currentActiveSeats(chargeSeats)
                .snapshotSeats(chargeSeats)
                .useSnapshotSeatsFirst(true)
                .action(ProrationInput.Action.UPGRADE) // 신규도 일관성을 위해 UPGRADE 경로 사용 가능(엔진 보호)
                .effective(ProrationInput.Effective.NOW)
                .activationMode(ProrationInput.ActivationMode.TOMORROW)
                .seatEvents(List.of())
                .baseFrom(next.start())
                .capEnd(today)
                .nextPeriodStart(next.start())
                .nextPeriodEndExcl(next.endExcl())
                .build();
    }

    // ===================== TX-1 =====================

    /**
     * TX-1: 인보이스 멱등 준비(아이템 채우고 OPEN) + PaymentAttempt(PENDING) INSERT
     */
    @Transactional
    protected Tx1Context tx1_prepareInvoiceAndBeginAttempt(ProrationInput input,
                                                           ProrationResult result,
                                                           String orderNumber) throws CustomException {
        // (A) LP 보장: 없으면 DRAFT로 생성
        Integer lpIdx = ensureLicensePartnershipForNewIfAbsent(input);

        // 1) 멱등 – orderNumber 중복 체크
        PaymentAttemptVO dup = paymentAttemptMapper.selectByOrderNumber(orderNumber);
        if (dup != null) {
            log.info("[tx1] duplicate orderNumber exists. attemptIdx={}", dup.getIdx());
            InvoiceVO inv = invoiceMapper.selectByIdx(dup.getInvoiceIdx());
            return Tx1Context.fromExistingAttempt(dup, inv, readDefaultMethodMandate(input.getPartnershipIdx()));
        }
        input.setLicensePartnershipIdx(lpIdx);
        // 2) 인보이스 멱등 upsert + OPEN
        InvoiceVO invoice = upsertDraftOpenInvoice(input, result);

        // 3) 기본 결제수단 + 활성 Mandate 조회
        DefaultMethodMandate mm = readDefaultMethodMandate(input.getPartnershipIdx());
        if (mm == null || mm.paymentMethodIdx() == null || mm.paymentMandateIdx() == null || mm.providerCd() == null) {
            throw new CustomException(ErrorCode.PAYMENT_NO_DEFAULT_METHOD);
        }

        // 4) 시도기록(PENDING)
        PaymentAttemptVO attempt = new PaymentAttemptVO();
        attempt.setInvoiceIdx(invoice.getIdx());
        attempt.setPartnershipIdx(input.getPartnershipIdx());
        attempt.setProviderCd(mm.providerCd());
        attempt.setPaymentMethodIdx(mm.paymentMethodIdx());
        attempt.setPaymentMandateIdx(mm.paymentMandateIdx());
        attempt.setAttemptNo(resolveNextAttemptNo(invoice.getIdx()));
        attempt.setAmount(BigDecimal.valueOf(result.getTotal()));
        attempt.setUnitCd("MUC0001");
        attempt.setStatusCd(EnumCode.PaymentAttempt.StateCd.PENDING.getCode());
        attempt.setOrderNumber(orderNumber);
        attempt.setMeta(buildAttemptMeta(input, result));
        paymentAttemptMapper.insertByPaymentAttemptVO(attempt);

        return new Tx1Context(invoice, attempt, mm);
    }

    private Integer ensureLicensePartnershipForNewIfAbsent(ProrationInput in) {
        var lpOpt = licensePartnershipMapper.selectByPartnershipIdx(in.getPartnershipIdx());
        if (lpOpt.isPresent()) return lpOpt.get().getIdx();

        LicensePartnershipVO lp = new LicensePartnershipVO();
        lp.setPartnershipIdx(in.getPartnershipIdx());
        lp.setLicenseIdx(in.getLicenseIdx());
        lp.setBillingDay(in.getNextPeriodStart().getDayOfMonth());
        lp.setPeriodStartDate(in.getNextPeriodStart());
        lp.setPeriodEndDate(in.getNextPeriodEndExcl());
        lp.setNextBillingDate(in.getNextPeriodEndExcl()); // 보통 = period_end_excl
        lp.setCurrentSeatCount(in.getCurrentActiveSeats());
        lp.setCurrentUnitPrice(in.getToPlan().getPricePerUser());
        lp.setCurrentMinUserCount(in.getMinChargeSeats());
        lp.setCancelAtPeriodEnd(false);
        lp.setStateCd(EnumCode.LicensePartnership.StateCd.DRAFT.getCode()); // DRAFT
        licensePartnershipMapper.insertByLicensePartnership(lp);  // useGeneratedKeys=true
        return lp.getIdx();
    }

    /**
     * 인보이스 멱등 생성(DRAFT) → 아이템 채우기 → 합계 재계산 → OPEN
     */
    @Transactional
    protected InvoiceVO upsertDraftOpenInvoice(ProrationInput input, ProrationResult result) {
        // 동일 LP + 동일 기간의 활성 인보이스가 있으면 재사용
        InvoiceVO exists = invoiceMapper.selectActiveByPeriod(
                input.getLicensePartnershipIdx(),
                input.getNextPeriodStart(),
                input.getNextPeriodEndExcl()
        );
        InvoiceVO inv = (exists != null) ? exists : createDraftInvoice(input);

        // 아이템 보장(엔진 결과 기반으로 PRORATION/RECURRING/CREDIT 채우기)
        ensureInvoiceItems(inv, input, result);

        // 합계/세금/총액 재계산
        invoiceMapper.recalcTotals(inv.getIdx(), BigDecimal.ZERO);

        // DRAFT → OPEN 1회성 전환(이미 OPEN이면 영향 없음)
        invoiceMapper.markOpen(inv.getIdx());

        return invoiceMapper.selectByIdx(inv.getIdx());
    }

    @Transactional
    protected InvoiceVO createDraftInvoice(ProrationInput input) {
        InvoiceVO inv = new InvoiceVO();
        inv.setPartnershipIdx(input.getPartnershipIdx());
        inv.setLicensePartnershipIdx(input.getLicensePartnershipIdx());
        inv.setPeriodStart(input.getNextPeriodStart());
        inv.setPeriodEnd(input.getNextPeriodEndExcl());
        inv.setSubtotal(BigDecimal.ZERO);
        inv.setTax(BigDecimal.ZERO);
        inv.setTotal(BigDecimal.ZERO);
        inv.setStatusCd(EnumCode.Invoice.StateCd.DRAFT.getCode());
        inv.setUnitCd("MUC0001");
        inv.setMeta(buildInvoiceMeta(input));
        invoiceMapper.insertByInvoiceVO(inv);
        return inv;
    }

    /**
     * 인보이스 아이템 채움 – 엔진 결과를 invoice_item으로 적재
     * (동일 항목이 이미 존재하면 중복 삽입하지 않도록 키/메타 기준으로 방어 로직을 추가할 수 있음)
     */
    @Transactional
    protected void ensureInvoiceItems(InvoiceVO inv, ProrationInput input, ProrationResult result) {
        // 예시: result.getLines()가 청구 항목 리스트라고 가정
        if (result.getItems() == null) return;

        for (ProrationResult.Item prorationItem : result.getItems()) {
            InvoiceItemVO item = new InvoiceItemVO();
            item.setInvoiceIdx(inv.getIdx());
            item.setItemTypeCd(mapItemType(prorationItem.getItemType()));
            item.setDescription(prorationItem.getDescription());
            item.setQuantity(prorationItem.getQuantity());
            item.setUnitPrice(BigDecimal.valueOf(prorationItem.getUnitPrice()));
            item.setDays(prorationItem.getDays());
            item.setAmount(BigDecimal.valueOf(prorationItem.getAmount()));
            item.setMeta(buildItemMeta(input, prorationItem));
            invoiceItemMapper.insertByInvoiceItemVO(item);
        }
    }

    // ===================== PG 호출 =====================

    /**
     * 비트랜잭션: 토스 정기결제 confirm 호출
     */
    protected JSONObject callPgConfirmBilling(Tx1Context tx1, ProrationResult result) throws CustomException {
        try {
            TossConfirmPayload tossConfirmPayload = TossConfirmPayload.of(
                    tx1.mandate().providerCd(),
                    tx1.mandate().paymentMethodIdx(),
                    tx1.mandate().paymentMandateIdx(),
                    tx1.attempt().getOrderNumber(),
                    BigDecimal.valueOf(result.getTotal())
            );
            JSONObject payload = new JSONObject(tossConfirmPayload);
            payload.put("billingKey", tx1.mandate.mandateId());
            payload.put("orderName", result.getPlanName());
            payload.put("orderId", tx1.attempt().getOrderNumber());
            payload.put("amount", tossConfirmPayload.amount());
            payload.put("customerKey", tx1.mandate.customerKey());
            return tossPaymentService.confirmBilling(payload);
        } catch (IOException ioe) {
            log.error("PG confirmBilling network error", ioe);
            // TX-2에서 실패 반영을 위해 예외로 넘김
            throw new CustomException(ErrorCode.PG_TOSS_PAYMENT_FAIL);
        }
    }

    // ===================== TX-2 =====================

    /**
     * TX-2: 응답 반영(시도 성공/실패) + 수납 기록 + 인보이스 상태 전이 + 구독(TOMORROW) 반영
     */
    @Transactional
    protected ResponsePaymentDTO.PaymentChargeResult tx2_finalizeAttemptAndReceipt(ProrationInput input,
                                                                                   ProrationResult result,
                                                                                   Tx1Context tx1,
                                                                                   JSONObject pgResp) throws CustomException {
        PaymentAttemptVO attempt = tx1.attempt();
        ZonedDateTime now = ZonedDateTime.now();

        // 1) 응답 검증 (금액/통화/성공여부)
        PgVerification vr = verifyPgResponse(pgResp, attempt.getOrderNumber(), BigDecimal.valueOf(result.getTotal()));

        // 2) 시도결과 업데이트
        attempt.setRespondDate(now);
        attempt.setMeta(pgResp == null ? null : pgResp.toString());

        if (!vr.success) {
            attempt.setStatusCd(EnumCode.PaymentAttempt.StateCd.FAILED.getCode());
            attempt.setFailureCode(vr.code);
            attempt.setFailureMessage(vr.message);
            paymentAttemptMapper.updateByPaymentAttemptVO(attempt);
            // 인보이스는 OPEN 유지 – 재시도 가능
            return ResponsePaymentDTO.PaymentChargeResult.failed(vr.code, vr.message);
        }

        attempt.setStatusCd(EnumCode.PaymentAttempt.StateCd.SUCCESS.getCode());
        paymentAttemptMapper.updateByPaymentAttemptVO(attempt);

        // 3) 수납 기록 (중복방지: (provider_cd, order_number) UNIQUE 권장)
        LicensePaymentHistoryVO pay = new LicensePaymentHistoryVO();
        pay.setInvoiceIdx(tx1.invoice().getIdx());
        pay.setProviderCd(tx1.mandate().providerCd());
        pay.setOrderNumber(attempt.getOrderNumber());

        pay.setAmount(BigDecimal.valueOf(result.getTotal()));
        pay.setUnitCd("MUC0001");
        pay.setMeta(pgResp == null ? null : pgResp.toString());
        pay.setPaidDate(now);
        licensePaymentHistoryMapper.insertByLicensePaymentHistoryVO(pay);

        // 4) 미납 계산 → 0원이면 PAID
        InvoicePaymentView summary = invoicePaymentViewMapper.selectInvoicePaymentSummary(tx1.invoice().getIdx());
        if (summary != null && summary.getBalanceDue() != null
                && summary.getBalanceDue().compareTo(BigDecimal.ZERO) <= 0) {
            invoiceMapper.markPaid(tx1.invoice().getIdx());
        }

        // 5) 정책: “오늘 결제, 내일부터 적용”
        applySubscriptionActivationAfterPaymentSuccess_TOMORROW(input);

        return ResponsePaymentDTO.PaymentChargeResult.ok(tx1.invoice(), attempt);
    }

    // ===================== 유틸리티/검증/매핑 =====================

    protected int resolveNextAttemptNo(Integer invoiceIdx) {
        // 간단 구현: 시도번호를 현재 timestamp 기반으로 1로 시작(권장: SELECT MAX(attempt_no)+1)
        // 필요 시 전용 Mapper 메서드를 만들어 MAX+1 가져오세요.
        return 1;
    }

    protected String buildInvoiceMeta(ProrationInput input) {
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("basis", "CALENDAR_DAYS");
        meta.put("periodStart", input.getNextPeriodStart());
        meta.put("periodEndExcl", input.getNextPeriodEndExcl());
        return new JSONObject(meta).toString();
    }

    protected String buildAttemptMeta(ProrationInput input, ProrationResult result) {
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("total", result.getTotal());
        meta.put("partnershipIdx", input.getPartnershipIdx());
        meta.put("licensePartnershipIdx", input.getLicensePartnershipIdx());
        return new JSONObject(meta).toString();
    }

    protected String buildItemMeta(ProrationInput input, ProrationResult.Item line) {
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("numeratorDays", line.getDays());
        meta.put("seatCount", line.getQuantity());
        meta.put("unitPrice", line.getUnitPrice());
        return new JSONObject(meta).toString();
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

    /**
     * 정책: “오늘 결제, 내일부터 적용”
     * 다음 주기(period_start/end)를 input에 이미 계산해두었고,
     * 성공 시 LP 스냅샷/기간을 내일자 주기로 갱신하는 메서드
     */
    @Transactional
    protected void applySubscriptionActivationAfterPaymentSuccess_TOMORROW(ProrationInput input) throws CustomException {
        // 예시: license_partnership의 period_start/end/next_billing_date, current_* 스냅샷 갱신
        // - input.getNextPeriodStart(), input.getNextPeriodEndExcl()
        // - input.getChargeSeats(), input.getUnitPriceSnapshot(), input.getMinUserCountSnapshot()
        LicensePartnershipVO lp = licensePartnershipMapper.selectByIdx(input.getLicensePartnershipIdx())
                .orElseThrow(() -> new CustomException(ErrorCode.COMMON_EMPTY));
        lp.setNextBillingDate(input.getNextPeriodEndExcl());
        lp.setCurrentUnitPrice(input.getToPlan().getPricePerUser());
        lp.setStateCd(EnumCode.LicensePartnership.StateCd.ACTIVE.getCode());
        lp.setCurrentSeatCount(input.getCurrentActiveSeats());
        lp.setPeriodStartDate(input.getNextPeriodStart());
        lp.setPeriodEndDate(input.getNextPeriodEndExcl());
        licensePartnershipMapper.updateByLicensePartnershipVO(lp);
    }

    // ===================== DTO/VO 보조 구조체 =====================

    /**
     * TX-1 결과 컨텍스트
     */
    protected record Tx1Context(InvoiceVO invoice, PaymentAttemptVO attempt, DefaultMethodMandate mandate) {
        static Tx1Context fromExistingAttempt(PaymentAttemptVO attempt, InvoiceVO invoice, DefaultMethodMandate mm) {
            return new Tx1Context(invoice, attempt, mm);
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

    private ResponsePaymentDTO.PaymentPreview buildPreviewByInputAndResult(ProrationInput input, ProrationResult result) {
        ResponsePaymentDTO.PaymentPreview response = new ResponsePaymentDTO.PaymentPreview();

        response.setTargetPlan(modelMapper.map(input.getToPlan(), ResponsePaymentDTO.PreviewPlan.class));
        response.setDenominatorDays(input.getDenominatorDays());
        response.setRoundingRule(input.getRoundingMode().name());
        response.setCurrency(input.getCurrency());
        response.setPeriodStart(input.getPeriodStart());
        response.setPeriodEnd(input.getPeriodEndExcl().minusDays(1)); // exclusive -> inclusive
        response.setOccurredAt(ZonedDateTime.now().toString());
        response.setChargeSeats(input.getCurrentActiveSeats());
        response.setSubTotal(result.getSubTotal());
        response.setTax(result.getTax());
        response.setTotal(result.getTotal());
        response.setCreditCarryOver(result.getCreditCarryOver());
        response.setItems(new ArrayList<>());
        for (ProrationResult.Item item : result.getItems()) {
            ResponsePaymentDTO.PreviewItem previewItem = new ResponsePaymentDTO.PreviewItem();
            previewItem.setItemType(item.getItemType());
            previewItem.setDescription(item.getDescription());
            previewItem.setQuantity(item.getQuantity());
            previewItem.setUnitPrice(item.getUnitPrice());
            previewItem.setDays(item.getDays());
            previewItem.setAmount(item.getAmount());
            previewItem.setRelatedEventId(item.getRelatedEventId());
            previewItem.setMeta(item.getMeta());
            response.getItems().add(previewItem);
        }
        return response;
    }

    /**
     * 정산 금액 계산 (프리뷰)
     * @param req
     * @param memberVO
     * @return
     * @throws CustomException
     */
    @Transactional(readOnly = true)
    public ResponsePaymentDTO.PaymentPreview calculateProrationAmount(RequestPaymentDTO.SubscriptionInfo req, MemberVO memberVO) throws CustomException {
        // LP 유무 확인
        Optional<LicensePartnershipVO> optLp = licensePartnershipMapper.selectByPartnershipIdx(req.getPartnershipIdx());
        final ProrationInput input = optLp.isEmpty()
                ? buildInputForPreview_NewSubscription(req)   // 신규 구독
                : buildInputForPreview(req, memberVO);        // 기존 구독(미청구/업그레이드 등)

        final ProrationResult result = prorationEngine.calculate(input);
        ResponsePaymentDTO.PaymentPreview response = buildPreviewByInputAndResult(input, result);
        response.setCurrentPlan(optLp.isPresent()
                ? modelMapper.map(licenseMapper.selectByIdx(optLp.get().getLicenseIdx())
                    .orElseThrow(() -> new CustomException(ErrorCode.COMMON_EMPTY)),
                    ResponsePaymentDTO.PreviewPlan.class)
                : null);
        response.setTargetPlan(optLp.isPresent()
                ? modelMapper.map(licenseMapper.selectByIdx(req.getLicenseIdx())
                        .orElseThrow(() -> new CustomException(ErrorCode.COMMON_EMPTY)),
                ResponsePaymentDTO.PreviewPlan.class)
                : null);
        return response;
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
                        .name(fromPlan.getName())
                        .build())
                .toPlan(ProrationInput.Plan.builder()
                        .idx(toPlan.getIdx())
                        .planCd(toPlan.getPlanCd())
                        .pricePerUser(toPlan.getPricePerUser())
                        .minUserCount(toPlan.getMinUserCount())
                        .name(toPlan.getName())
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

    private ProrationInput buildInputForCharge_Tomorrow(RequestPaymentDTO.SubscriptionChangeEvent req, MemberVO member) throws CustomException {
        int partnershipIdx = req.getPartnershipIdx();
        var lp   = licensePartnershipMapper.selectByPartnershipIdx(partnershipIdx)
                .orElseThrow(() -> new CustomException(ErrorCode.LICENSE_PARTNERSHIP_EMPTY));
        var from = licenseMapper.selectByIdx(lp.getLicenseIdx())
                .orElseThrow(() -> new CustomException(ErrorCode.LICENSE_PARTNERSHIP_EMPTY));
        var to   = licenseMapper.selectByIdx(req.getLicenseIdx())
                .orElseThrow(() -> new CustomException(ErrorCode.LICENSE_PARTNERSHIP_EMPTY));

        var lastInv = invoiceMapper.selectLastIssuedByLicensePartnershipIdx(lp.getIdx()).orElse(null);
        int prepaidSeats = resolvePrepaidSeatsFromLastRecurring(lastInv, lp);

        LocalDate today = ZonedDateTime.now().toLocalDate();
        LocalDate tomorrow = today.plusDays(1);

        // 신규 주기 기간(내일부터 1개월) — 기존 앵커 규칙 재사용 권장
        int billingDay = resolveBillingDayAnchor(tomorrow, lp); // 보통 tomorrow.dayOfMonth 또는 lp.billing_day
        var nextPeriod = resolvePeriodFromAnchor(tomorrow, billingDay); // start=tomorrow, endExcl=tomorrow+월
        // 구 플랜 미청구 분모: 기존 주기 분모(예: 31)
        int Dold = (int) DAYS.between(lp.getPeriodStartDate(), lp.getPeriodEndDate());
        // 신 주기 표시용 분모
        int Dnew = (int) DAYS.between(nextPeriod.start(), nextPeriod.endExcl());

        // 좌석 이벤트(미청구): baseFrom ~ today(어제까지) 사이만
        LocalDate baseFrom = (lastInv != null) ? lastInv.getIssueDate().toLocalDate() : lp.getPeriodStartDate();
        var evts = subscriptionChangeEventMapper.selectByLpAndOccurredBetween(lp.getIdx(), baseFrom, today);
        var seatEvents = evts.stream().map(e -> ProrationInput.SeatEvent.builder()
                        .date(e.getOccurredDate().toLocalDate())
                        .delta(e.getQtyDelta())
                        .relatedId(e.getIdx().longValue())
                        .build())
                .toList();

        int active = partnershipComponent.getPartnershipActiveMemberCount(partnershipIdx);
        Integer snapshotSeats = lp.getCurrentSeatCount();

        return ProrationInput.builder()
                .partnershipIdx(partnershipIdx)
                .licenseIdx(to.getIdx())
                .licensePartnershipIdx(lp.getIdx())
                .periodStart(lp.getPeriodStartDate())        // 구 주기
                .periodEndExcl(lp.getPeriodEndDate())
                .today(today)
                .denominatorDays(Dold)
                .denominatorDaysOld(Dold)
                .denominatorDaysNew(Dnew)
                .roundingMode(RoundingMode.HALF_UP)
                .currency("KRW")
                .fromPlan(ProrationInput.Plan.builder()
                        .idx(from.getIdx()).planCd(from.getPlanCd())
                        .pricePerUser(from.getPricePerUser())
                        .minUserCount(from.getMinUserCount())
                        .name(from.getName())
                        .build())
                .toPlan(ProrationInput.Plan.builder()
                        .idx(to.getIdx()).planCd(to.getPlanCd())
                        .pricePerUser(to.getPricePerUser())
                        .minUserCount(to.getMinUserCount())
                        .name(to.getName())
                        .build())
                .prepaidSeats(prepaidSeats)
                .minChargeSeats(from.getMinUserCount())
                .currentActiveSeats(active)
                .useSnapshotSeatsFirst(true)   // 결제 시점의 좌석 스냅샷 우선(당일 변동 반영)
                .snapshotSeats(snapshotSeats)
                .action(ProrationInput.Action.UPGRADE)
                .effective(ProrationInput.Effective.NOW) // 액션 자체는 NOW지만,
                .activationMode(ProrationInput.ActivationMode.TOMORROW) // 내일부터 효력
                .seatEvents(seatEvents)         // 어제까지 이벤트만 반영
                .baseFrom(baseFrom)
                .capEnd(today)                  // 미청구 스캔 종료 = 오늘(배타)
                .nextPeriodStart(nextPeriod.start())
                .nextPeriodEndExcl(nextPeriod.endExcl())
                .build();
    }
    // Period 표현용 (endExcl = 배타)
    public record PeriodRange(LocalDate start, LocalDate endExcl) {}

    /**
     * 기존 앵커(권장)를 우선 재사용하되, 없으면 baseDate의 dayOfMonth를 28로 clamp.
     * 입력이 29~31이더라도 28에 고정하여 모든 달에서 유효하게 유지합니다.
     */
    private int resolveBillingDayAnchor(LocalDate baseDate, @Nullable Integer preferredAnchor) {
        // 1) 우선 기존 앵커 재사용 (권장 범위: 1~28)
        if (preferredAnchor != null && preferredAnchor >= 1) {
            return Math.min(preferredAnchor, 28);
        }
        // 2) 없으면 baseDate의 일자를 28로 clamp
        return Math.min(baseDate.getDayOfMonth(), 28);
    }

    /**
     * 오버로드: LP를 알고 있으면 그걸 넘겨서 재사용
     */
    private int resolveBillingDayAnchor(LocalDate baseDate, LicensePartnershipVO lp) {
        return resolveBillingDayAnchor(baseDate, lp.getBillingDay());
    }

    /**
     * baseDate(=내일)부터 시작해서, "다음 앵커일자"를 endExcl로 반환.
     * - baseDate의 일이 앵커보다 작은 경우: 같은 달의 앵커일 = endExcl
     * - baseDate의 일이 앵커 이상인 경우: 다음 달의 앵커일 = endExcl
     * 모든 달 길이를 고려해 (예: 2월) 앵커일이 그 달 길이보다 크면 마지막 날로 clamp.
     */
    private PeriodRange resolvePeriodFromAnchor(LocalDate baseDate, int billingDay) {
        if (billingDay < 1) billingDay = 1;
        if (billingDay > 28) billingDay = 28; // 설계 원칙: 1~28 권장

        LocalDate start = baseDate;

        // 같은 달에 앵커가 아직 남아있으면 그날, 없으면 다음 달의 앵커
        LocalDate candidate;
        if (start.getDayOfMonth() < billingDay) {
            candidate = withClampedDay(start, billingDay);
        } else {
            LocalDate nextMonth = start.plusMonths(1);
            candidate = withClampedDay(nextMonth, billingDay);
        }

        LocalDate endExcl = candidate; // 종료=배타
        return new PeriodRange(start, endExcl);
    }

    /**
     * 해당 달 길이를 초과하는 day를 달의 마지막 날로 클램프.
     * 예) 2월 + billingDay=28 (OK), billingDay=30 -> 2월의 말일로.
     */
    private LocalDate withClampedDay(LocalDate anyDate, int day) {
        YearMonth ym = YearMonth.from(anyDate);
        int last = ym.lengthOfMonth();
        int d = Math.min(Math.max(day, 1), last);
        return LocalDate.of(ym.getYear(), ym.getMonth(), d);
    }

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

    /**
     * 신규 구독 프리뷰용 입력 빌더
     * @param req
     * @return
     * @throws CustomException
     */
    private ProrationInput buildInputForPreview_NewSubscription(RequestPaymentDTO.SubscriptionInfo req) throws CustomException {
        final int partnershipIdx = req.getPartnershipIdx();
        final LicenseVO toPlan = licenseMapper.selectByIdx(req.getLicenseIdx())
                .orElseThrow(() -> new CustomException(ErrorCode.COMMON_EMPTY));

        final LocalDate today = ZonedDateTime.now().toLocalDate();
        final LocalDate tomorrow = today.plusDays(1);

        // 앵커 계산(신규): 내일부터 시작, 1~28 클램프
        final int billingDay = resolveBillingDayAnchor(tomorrow, (Integer) null);
        final PeriodRange nextPeriod = resolvePeriodFromAnchor(tomorrow, billingDay);

        // 좌석 산정: 활성 멤버 vs 최소과금
        final int active = partnershipComponent.getPartnershipActiveMemberCount(partnershipIdx);
        final int minNew = toPlan.getMinUserCount() == null ? 0 : toPlan.getMinUserCount();
        final int seatsForRecurring = Math.max(active, minNew);

        final int Dnew = (int) DAYS.between(nextPeriod.start(), nextPeriod.endExcl());

        return ProrationInput.builder()
                // 구 주기는 없지만, 엔진 호환을 위해 nextPeriod를 표시용으로 매핑
                .periodStart(nextPeriod.start())
                .periodEndExcl(nextPeriod.endExcl())
                .today(today)
                .denominatorDays(Dnew)
                .denominatorDaysNew(Dnew)
                .roundingMode(RoundingMode.HALF_UP)
                .currency("KRW")
                .fromPlan(null)
                .toPlan(ProrationInput.Plan.builder()
                        .idx(toPlan.getIdx())
                        .planCd(toPlan.getPlanCd())
                        .pricePerUser(toPlan.getPricePerUser())
                        .minUserCount(toPlan.getMinUserCount())
                        .name(toPlan.getName())
                        .build())
                .prepaidSeats(0)                    // 선불 좌석 없음
                .minChargeSeats(minNew)
                .currentActiveSeats(seatsForRecurring)
                .snapshotSeats(seatsForRecurring)    // 프리뷰 스냅샷 = 산정 좌석
                .useSnapshotSeatsFirst(true)
                .action(ProrationInput.Action.UPGRADE) // 엔진 재사용을 위해 UPGRADE로 통일(NEW 액션이 없다면)
                .effective(ProrationInput.Effective.NOW)
                .activationMode(ProrationInput.ActivationMode.TOMORROW)
                .seatEvents(java.util.List.of())     // 이벤트 없음
                .baseFrom(nextPeriod.start())        // 의미상 next period 기준
                .capEnd(today)
                .nextPeriodStart(nextPeriod.start())
                .nextPeriodEndExcl(nextPeriod.endExcl())
                .build();
    }
}
