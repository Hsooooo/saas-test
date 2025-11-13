package com.illunex.emsaasrestapi.license.mapper;

import com.illunex.emsaasrestapi.license.vo.LicenseVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mapper
public interface LicenseMapper {
    Optional<LicenseVO> selectByIdx(int idx);
    List<LicenseVO> selectAllByActive();

    LicenseVO selectByPlanCdAndActive(String code);
}
