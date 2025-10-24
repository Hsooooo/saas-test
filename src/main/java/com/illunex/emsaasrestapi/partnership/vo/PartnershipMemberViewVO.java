package com.illunex.emsaasrestapi.partnership.vo;

import com.illunex.emsaasrestapi.common.code.EnumCode;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.time.ZonedDateTime;

@Getter
@Setter
@Alias("PartnershipMemberViewVO")
public class PartnershipMemberViewVO {
    private Integer partnershipMemberIdx;
    private String name;
    private String profileImageUrl;
    private String profileImagePath;
    private String email;
    private String stateCd;
    private String managerCd;
    private String teamName;
    private String partnershipTeamIdx;
    private String inviteMemberName;
    private Integer invitedByPartnershipMemberIdx;
    private ZonedDateTime joinedDate;
    private ZonedDateTime invitedDate;
}
