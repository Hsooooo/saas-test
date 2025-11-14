package com.illunex.emsaasrestapi.license;

import com.illunex.emsaasrestapi.common.CustomException;
import com.illunex.emsaasrestapi.common.ErrorCode;
import com.illunex.emsaasrestapi.common.code.EnumCode;
import com.illunex.emsaasrestapi.license.dto.ResponseLicenseDTO;
import com.illunex.emsaasrestapi.license.mapper.LicenseMapper;
import com.illunex.emsaasrestapi.license.mapper.LicensePartnershipMapper;
import com.illunex.emsaasrestapi.license.vo.LicensePartnershipVO;
import com.illunex.emsaasrestapi.license.vo.LicenseVO;
import com.illunex.emsaasrestapi.member.vo.MemberVO;
import com.illunex.emsaasrestapi.partnership.PartnershipComponent;
import com.illunex.emsaasrestapi.payment.mapper.SubscriptionChangeEventMapper;
import com.illunex.emsaasrestapi.payment.vo.SubscriptionChangeEventVO;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LicenseService {
    private final LicenseMapper licenseMapper;
    private final ModelMapper modelMapper;
    private final PartnershipComponent partnershipComponent;
    private final LicensePartnershipMapper licensePartnershipMapper;
    private final SubscriptionChangeEventMapper subscriptionChangeEventMapper;

    /**
     * 라이선스 목록 조회
     * @return
     */
    public ResponseLicenseDTO.LicenseList getLicenses() {
        List<LicenseVO> licenses = licenseMapper.selectAllByActive();
        List<ResponseLicenseDTO.License> licenseList = modelMapper.map(licenses, new TypeToken<List<ResponseLicenseDTO.License>>() {}.getType());

        return new ResponseLicenseDTO.LicenseList() {{
            setLicenseList(licenseList);
        }};
    }

    public ResponseLicenseDTO.PartnershipLicenseInfo getLicenseInfo(Integer partnershipIdx, MemberVO memberVO) throws CustomException {
        // 파트너쉽 회원 체크
        partnershipComponent.checkPartnershipMember(memberVO, partnershipIdx);
        ResponseLicenseDTO.PartnershipLicenseInfo resp = new ResponseLicenseDTO.PartnershipLicenseInfo();

        LicensePartnershipVO licensePartnershipVO = licensePartnershipMapper.selectByPartnershipIdx(partnershipIdx).orElse(null);
        ResponseLicenseDTO.License license = null;
        if (licensePartnershipVO != null) {
            LicenseVO licenseVO = licenseMapper.selectByIdx(licensePartnershipVO.getLicenseIdx()).orElse(null);
            if (licenseVO != null) {
                license = modelMapper.map(licenseVO, ResponseLicenseDTO.License.class);
            } else {
                license = new ResponseLicenseDTO.License();
                license.setPlanCd(EnumCode.License.PlanCd.BASIC.getCode());
            }
            resp.setPeriodStartDate(licensePartnershipVO.getPeriodStartDate());
            resp.setPeriodEndDate(licensePartnershipVO.getPeriodEndDate());
            resp.setStateCd(licensePartnershipVO.getStateCd());
            if (licensePartnershipVO.getStateCd().equals(EnumCode.LicensePartnership.StateCd.CHANGE.getCode())) {
                SubscriptionChangeEventVO subscriptionChangeEventVO = subscriptionChangeEventMapper
                        .selectLastOneByLpAndOccurredBetweenAndTypeCd(licensePartnershipVO.getIdx(),
                                licensePartnershipVO.getPeriodStartDate().atStartOfDay(),
                                licensePartnershipVO.getPeriodEndDate().atStartOfDay(),
                                EnumCode.SubscriptionChangeEvent.TypeCd.PLAN_DOWNGRADE.getCode());
                if (subscriptionChangeEventVO != null) {
                    LicenseVO nextLicenseVO = licenseMapper.selectByIdx(subscriptionChangeEventVO.getToLicenseIdx()).orElse(null);
                    if (nextLicenseVO != null) {
                        ResponseLicenseDTO.License nextLicense = modelMapper.map(nextLicenseVO, ResponseLicenseDTO.License.class);
                        resp.setNextLicense(nextLicense);
                    }
                }
            } else if (licensePartnershipVO.getStateCd().equals(EnumCode.LicensePartnership.StateCd.CANCEL.getCode())) {
                // 만료된 라이선스인 경우 기본 라이선스로 세팅
                license = new ResponseLicenseDTO.License();
                license.setPlanCd(EnumCode.License.PlanCd.BASIC.getCode());
            }
        } else {
            license = new ResponseLicenseDTO.License();
            license.setPlanCd(EnumCode.License.PlanCd.BASIC.getCode());
        }
        resp.setLicense(license);
        resp.setPartnershipIdx(partnershipIdx);

        return resp;
    }
}
