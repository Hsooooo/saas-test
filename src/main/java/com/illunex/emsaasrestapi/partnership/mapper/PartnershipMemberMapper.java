package com.illunex.emsaasrestapi.partnership.mapper;

import com.illunex.emsaasrestapi.partnership.vo.PartnershipInvitedMemberVO;
import com.illunex.emsaasrestapi.partnership.vo.PartnershipMemberVO;
import com.illunex.emsaasrestapi.partnership.vo.PartnershipVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface PartnershipMemberMapper {
    Integer insertByPartnershipMember(PartnershipMemberVO partnershipMemberVO);
    Boolean isMemberManagerOfPartnership(Integer partnershipIdx, Integer memberIdx);
    Boolean isDuplicateMemberByEmail(Integer partnershipIdx, String email);
    Optional<PartnershipMemberVO> selectByPartnershipIdxAndMemberIdx(Integer partnershipIdx, Integer memberIdx);
    Boolean existsInvitedMember(Integer partnershipIdx, String email);
    void insertInvitedMember(PartnershipInvitedMemberVO partnershipInvitedMemberVO);
    List<PartnershipMemberVO> selectAllByProjectIdx(Integer projectIdx);
    int updatePositionIdxAndPhoneByIdx(@Param("positionIdx") Integer positionIdx, @Param("phone") String phone, @Param("idx") Integer partnershipMemberIdx);
    void updateProfileImageByIdx(PartnershipMemberVO partnershipMember);
}
