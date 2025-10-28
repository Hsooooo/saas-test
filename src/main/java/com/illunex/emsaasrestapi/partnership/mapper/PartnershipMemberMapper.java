package com.illunex.emsaasrestapi.partnership.mapper;

import com.illunex.emsaasrestapi.partnership.vo.PartnershipInvitedMemberVO;
import com.illunex.emsaasrestapi.partnership.vo.PartnershipMemberVO;
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
    int updatePositionIdxAndPhoneByIdx(@Param("positionIdx") Integer positionIdx, @Param("phone") String phone, @Param("idx") Integer partnershipMemberIdx);
    void updateProfileImageByIdx(PartnershipMemberVO partnershipMember);
    Optional<PartnershipMemberVO> selectByIdx(int partnershipMemberIdx);
    void updatePartnershipMemberStateByIdx(Integer idx, String stateCd);
    void updatePartnershipMemberManagerCdByIdx(Integer idx, String managerCd);
    List<PartnershipMemberVO> selectAllByPartnershipIdx(Integer partnershipIdx);
    List<PartnershipMemberVO> selectByProjectIdx(Integer projectIdx);

    Integer countByPartnershipIdxAndNotStateCd(Integer partnershipIdx, String code);
}
