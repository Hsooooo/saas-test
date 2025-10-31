package com.illunex.emsaasrestapi.payment.mapper;

import com.illunex.emsaasrestapi.payment.vo.PaymentAttemptVO;
import com.illunex.emsaasrestapi.payment.vo.PaymentMandateVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

@Mapper
public interface PaymentAttemptMapper {
    void insertByPaymentAttemptVO(PaymentAttemptVO paymentAttemptVO);
    PaymentAttemptVO selectByOrderNumber(String orderNumber);

    void updateByPaymentAttemptVO(PaymentAttemptVO paymentAttemptVO);
}
