package com.illunex.emsaasrestapi.partnership.vo;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.time.ZonedDateTime;

@Getter
@Setter
@Alias("PartnershipInvitedMemberVO")
public class PartnershipInvitedMemberVO {
    private Integer idx;
    private String email;

    private Integer partnershipIdx;

    private Integer invitedByPartnershipMemberIdx;
    private Integer memberIdx;
    private Integer partnershipMemberIdx;

    private ZonedDateTime invitedDate;
    private ZonedDateTime joinedDate;
}
