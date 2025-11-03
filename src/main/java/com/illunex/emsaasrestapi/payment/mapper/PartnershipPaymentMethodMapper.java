package com.illunex.emsaasrestapi.payment.mapper;

import com.illunex.emsaasrestapi.payment.vo.PartnershipPaymentMethodVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

@Mapper
public interface PartnershipPaymentMethodMapper {
    void insertByPartnershipPaymentMethodVO(PartnershipPaymentMethodVO methodVO);

    Optional<PartnershipPaymentMethodVO> selectDefaultByPartnershipIdx(int partnershipIdx);

    Optional<PartnershipPaymentMethodVO> selectByCustomerKey(String customerKey);

    void updateByPartnershipPaymentMethodVO(PartnershipPaymentMethodVO methodVO);
}
