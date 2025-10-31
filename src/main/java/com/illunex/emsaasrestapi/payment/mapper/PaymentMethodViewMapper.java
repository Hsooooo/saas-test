package com.illunex.emsaasrestapi.payment.mapper;

import com.illunex.emsaasrestapi.payment.vo.PaymentMethodViewVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PaymentMethodViewMapper {
    List<PaymentMethodViewVO> selectPaymentMethodsByPartnershipIdx(Integer partnershipIdx);
}
