package com.illunex.emsaasrestapi.payment.mapper;

import com.illunex.emsaasrestapi.payment.vo.PartnershipPaymentMethodVO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PartnershipPaymentMethodMapper {
    void insertByPartnershipPaymentMethodVO(PartnershipPaymentMethodVO methodVO);
}
