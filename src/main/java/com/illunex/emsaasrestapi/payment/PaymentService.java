package com.illunex.emsaasrestapi.payment;

import com.illunex.emsaasrestapi.common.CustomException;
import com.illunex.emsaasrestapi.license.mapper.LicenseMapper;
import com.illunex.emsaasrestapi.license.mapper.LicensePartnershipMapper;
import com.illunex.emsaasrestapi.license.vo.LicenseVO;
import com.illunex.emsaasrestapi.member.vo.MemberVO;
import com.illunex.emsaasrestapi.partnership.PartnershipComponent;
import com.illunex.emsaasrestapi.partnership.vo.PartnershipMemberVO;
import com.illunex.emsaasrestapi.payment.dto.RequestPaymentDTO;
import com.illunex.emsaasrestapi.payment.mapper.InvoiceMapper;
import com.illunex.emsaasrestapi.payment.mapper.InvoicePaymentViewMapper;
import com.illunex.emsaasrestapi.payment.mapper.PartnershipPaymentMethodMapper;
import com.illunex.emsaasrestapi.payment.mapper.PaymentMandateMapper;
import com.illunex.emsaasrestapi.payment.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

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
}
