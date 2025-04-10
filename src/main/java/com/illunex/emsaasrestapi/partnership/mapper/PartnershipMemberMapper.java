package com.illunex.emsaasrestapi.partnership.mapper;

import com.illunex.emsaasrestapi.partnership.vo.PartnershipMemberVO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PartnershipMemberMapper {
    Integer insertByPartnershipMemberJoin(PartnershipMemberVO partnershipMemberVO);
}
