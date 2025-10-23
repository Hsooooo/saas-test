package com.illunex.emsaasrestapi.payment;

import com.illunex.emsaasrestapi.common.CurrentMember;
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
}
