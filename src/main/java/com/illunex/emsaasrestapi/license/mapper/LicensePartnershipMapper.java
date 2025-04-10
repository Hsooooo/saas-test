package com.illunex.emsaasrestapi.license.mapper;

import com.illunex.emsaasrestapi.license.vo.LicensePartnershipVO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LicensePartnershipMapper {
    Integer insertByLicensePartnership(LicensePartnershipVO vo);
}
