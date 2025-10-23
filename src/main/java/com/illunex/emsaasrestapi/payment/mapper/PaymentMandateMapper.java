package com.illunex.emsaasrestapi.payment.mapper;

import com.illunex.emsaasrestapi.payment.vo.PaymentMandateVO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PaymentMandateMapper {
    void insertByPaymentMandateVO(PaymentMandateVO paymentMandateVO);
}
