package com.illunex.emsaasrestapi.license.mapper;

import com.illunex.emsaasrestapi.license.vo.LicensePartnershipVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface LicensePartnershipMapper {
    Integer insertByLicensePartnership(LicensePartnershipVO vo);

    List<LicensePartnershipVO> selectByNextBillingDateNow();

    List<Integer> selectIdxsByNextBillingDateToday();

    LicensePartnershipVO selectByIdxForUpdate(Integer idx);

    void updateByLicensePartnershipVO(LicensePartnershipVO lp);

    void updatePeriod(LicensePartnershipVO lp);
}
