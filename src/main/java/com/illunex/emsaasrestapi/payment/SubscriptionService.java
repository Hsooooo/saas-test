package com.illunex.emsaasrestapi.payment;

import com.illunex.emsaasrestapi.license.mapper.LicensePartnershipMapper;
import com.illunex.emsaasrestapi.license.vo.LicensePartnershipVO;
import com.illunex.emsaasrestapi.payment.mapper.SubscriptionChangeEventMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private final SubscriptionChangeEventMapper subscriptionChangeEventMapper;
    private final LicensePartnershipMapper licensePartnershipMapper;



    public void partnershipAddSeatEvent(Integer partnershipIdx, Integer licenseIdx) {
//        LicensePartnershipVO lp = licensePartnershipMapper.selectByPartnershipIdx(partnershipIdx)
//                .orElseThrow(() -> new IllegalArgumentException("LicensePartnership not found for partnershipIdx: " + partnershipIdx));

//        subscriptionChangeEventMapper.insertPartnershipAddSeatEvent(partnershipIdx, licenseIdx);
    }



}
