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

    @PostMapping("/toss/method/register")
    public String registerPaymentMethod(@RequestBody RequestPaymentDTO.MethodRegister request,
                                        @CurrentMember MemberVO memberVO) throws Exception {
        paymentService.registerPaymentMethodToss(request, memberVO);
        return "Payment method registered!";
    }

    @PostMapping("/subscription/change-event")
    public String subscriptionChangeEvent(@RequestBody RequestPaymentDTO.SubscriptionChangeEvent request,
                                          @CurrentMember MemberVO memberVO) throws Exception {
//        paymentService.handleSubscriptionChangeEvent(request, memberVO);
        return "Subscription change event handled!";
    }

    @PostMapping("/subscription/change-event/calc-proration")
    public CustomResponse<?> calculateProration(@RequestBody RequestPaymentDTO.SubscriptionInfo subscriptionInfo,
                                                @CurrentMember MemberVO memberVO) throws Exception {
        return CustomResponse.builder()
                .data(paymentService.calculateProrationAmount(subscriptionInfo, memberVO))
                .build();
    }
}
