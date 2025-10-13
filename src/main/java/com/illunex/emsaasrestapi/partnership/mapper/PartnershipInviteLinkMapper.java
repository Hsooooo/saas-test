package com.illunex.emsaasrestapi.partnership.mapper;

import com.illunex.emsaasrestapi.partnership.vo.PartnershipInviteLinkVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

@Mapper
public interface PartnershipInviteLinkMapper {
    void insertByPartnershipInviteLinkVO(PartnershipInviteLinkVO partnershipInviteLinkVO);
    Optional<PartnershipInviteLinkVO> selectByInviteTokenHash(String inviteTokenHash);
    void updateByPartnershipInviteLinkVO(PartnershipInviteLinkVO partnershipInviteLinkVO);
}
