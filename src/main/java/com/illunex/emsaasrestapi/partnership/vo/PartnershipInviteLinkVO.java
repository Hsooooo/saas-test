package com.illunex.emsaasrestapi.partnership.vo;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.time.ZonedDateTime;

@Getter
@Setter
@Alias("PartnershipInviteLinkVO")
public class PartnershipInviteLinkVO {
    private Integer idx;
    private Integer partnershipIdx;
    private Integer createdByPartnershipMemberIdx;
    private String inviteTokenHash;
    private Integer usedCount;
    private String stateCd;
    private String inviteInfoJson;
    private ZonedDateTime expireDate;
    private ZonedDateTime createDate;
}
