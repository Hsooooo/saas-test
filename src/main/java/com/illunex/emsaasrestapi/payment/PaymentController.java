package com.illunex.emsaasrestapi.payment;

import com.illunex.emsaasrestapi.common.CurrentMember;
import com.illunex.emsaasrestapi.common.CustomResponse;
import com.illunex.emsaasrestapi.member.vo.MemberVO;
import com.illunex.emsaasrestapi.payment.dto.RequestPaymentDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("payment")
@Slf4j
public class PaymentController {
    private final PaymentSchedule paymentSchedule;
    private final PaymentService paymentService;
    @GetMapping("/test")
    public String test() {
        paymentSchedule.generateMonthlyInvoiceBatch();
        return "Payment API is working!";
    }

    /**
     * customerKey 발급
     * @param memberVO
     * @return
     * @throws Exception
     */
    @GetMapping("/customer-key/generate")
    public CustomResponse<?> generateCustomerKey(@CurrentMember MemberVO memberVO) throws Exception {
        return CustomResponse.builder()
                .data(paymentService.createCustomerKey())
                .build();
    }

    /**
     * 자동결제용 빌링키 등록
     * @param request
     * @param memberVO
     * @return
     * @throws Exception
     */
    @PostMapping("/toss/method/register")
    public CustomResponse<?> registerPaymentMethod(@RequestBody RequestPaymentDTO.MethodRegister request,
                                        @CurrentMember MemberVO memberVO) throws Exception {
        return CustomResponse.builder()
                .data(paymentService.registerPaymentMethodToss(request, memberVO))
                .build();
    }

    @GetMapping("/toss/method")
    public CustomResponse<?> getPaymentMethod(@RequestParam Integer partnershipIdx,
                                              @CurrentMember MemberVO memberVO) throws Exception {
        return CustomResponse.builder()
                .data(paymentService.getPaymentMethodToss(partnershipIdx, memberVO))
                .build();
    }

    /**
     * 구독 변경 이벤트 청구 금액 계산
     * @param subscriptionInfo
     * @param memberVO
     * @return
     * @throws Exception
     */
    @PostMapping("/subscription/change-event/calc-proration")
    public CustomResponse<?> calculateProration(@RequestBody RequestPaymentDTO.SubscriptionInfo subscriptionInfo,
                                                @CurrentMember MemberVO memberVO) throws Exception {
        return CustomResponse.builder()
                .data(paymentService.calculateProrationAmount(subscriptionInfo, memberVO))
                .build();
    }

    /**
     * 구독 변경 이벤트 즉시 청구 적용
     * @param request
     * @param memberVO
     * @return
     * @throws Exception
     */
    @PostMapping("/subscription/change-event/apply")
    public CustomResponse<?> applySubscriptionChangeEvent(@RequestBody RequestPaymentDTO.SubscriptionInfo request,
                                               @CurrentMember MemberVO memberVO) throws Exception {
        return CustomResponse.builder()
                .data(paymentService.chargeNow(request, memberVO))
                .build();
    }
}
