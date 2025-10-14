package com.illunex.emsaasrestapi.partnership.vo;

import org.apache.ibatis.type.Alias;

import java.time.ZonedDateTime;

@Alias("PartnershipMemberViewVO")
public class PartnershipMemberViewVO {
    private Integer partnershipMemberIdx;
    private String name;
    private String email;
    private String stateCd;
    private String partnershipTeamIdx;
    private String invitedMember;
    private ZonedDateTime invitedDate;
    private ZonedDateTime joinedDate;
}
