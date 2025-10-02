package com.illunex.emsaasrestapi.partnership.mapper;

import com.illunex.emsaasrestapi.partnership.vo.PartnershipInvitedMemberVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

@Mapper
public interface PartnershipInvitedMemberMapper {
    void insertInvitedMember(PartnershipInvitedMemberVO partnershipInvitedMemberVO);
    Boolean existsInvitedMember(Integer partnershipIdx, String email);

    List<PartnershipInvitedMemberVO> selectAllByPartnershipIdx(Integer partnershipIdx);

    Optional<PartnershipInvitedMemberVO> selectByPartnershipMemberIdx(Integer idx);

    void updateJoinedDateNowByIdx(Integer idx);
}
