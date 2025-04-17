package com.illunex.emsaasrestapi.partnership.mapper;

import com.illunex.emsaasrestapi.partnership.vo.PartnershipInvitedMemberVO;
import com.illunex.emsaasrestapi.partnership.vo.PartnershipMemberVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

@Mapper
public interface PartnershipMemberMapper {
    Integer insertByPartnershipMember(PartnershipMemberVO partnershipMemberVO);
    Boolean isMemberManagerOfPartnership(Integer partnershipIdx, Integer memberIdx);
    Boolean isDuplicateMemberByEmail(Integer partnershipIdx, String email);
    Optional<PartnershipMemberVO> selectPartnershipMemberByMemberIdx(Integer partnershipIdx, Integer memberIdx);
    Boolean existsInvitedMember(Integer partnershipIdx, String email);
    void insertInvitedMember(PartnershipInvitedMemberVO partnershipInvitedMemberVO);
}
