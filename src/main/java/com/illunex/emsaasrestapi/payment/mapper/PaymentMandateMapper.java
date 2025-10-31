package com.illunex.emsaasrestapi.payment.mapper;

import com.illunex.emsaasrestapi.payment.vo.PaymentMandateVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

@Mapper
public interface PaymentMandateMapper {
    void insertByPaymentMandateVO(PaymentMandateVO paymentMandateVO);
    Optional<PaymentMandateVO> selectLastOneByPaymentMethodIdx(int paymentMethodIdx);
}
