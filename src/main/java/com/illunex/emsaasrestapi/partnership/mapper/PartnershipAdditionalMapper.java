package com.illunex.emsaasrestapi.partnership.mapper;

import com.illunex.emsaasrestapi.partnership.vo.PartnershipAdditionalVO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PartnershipAdditionalMapper {
    void insertByPartnershipAdditionalVO(PartnershipAdditionalVO partnershipAdditionalVO);
    void deleteByPartnershipIdx(Integer partnershipIdx);
}
