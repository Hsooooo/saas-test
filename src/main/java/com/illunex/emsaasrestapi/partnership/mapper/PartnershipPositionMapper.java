package com.illunex.emsaasrestapi.partnership.mapper;

import com.illunex.emsaasrestapi.partnership.vo.PartnershipInvitedMemberVO;
import com.illunex.emsaasrestapi.partnership.vo.PartnershipMemberVO;
import com.illunex.emsaasrestapi.partnership.vo.PartnershipPositionVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

@Mapper
public interface PartnershipPositionMapper {
    Integer insertByPartnershipPositionVO(PartnershipPositionVO partnershipPositionVO);
    Optional<PartnershipPositionVO> selectByNameAndPartnershipIdx(Integer partnershipIdx, String name);
    Integer selectMaxSortLevelByPartnershipIdx(Integer partnershipIdx);
    Optional<PartnershipPositionVO> selectByIdx(Integer idx);
}
