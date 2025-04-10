package com.illunex.emsaasrestapi.partnership.mapper;

import com.illunex.emsaasrestapi.partnership.vo.PartnershipVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

@Mapper
public interface PartnershipMapper {
    Optional<PartnershipVO> selectByDomain(String domain);
    Integer insertByPartnerJoin(PartnershipVO partnership);
}
