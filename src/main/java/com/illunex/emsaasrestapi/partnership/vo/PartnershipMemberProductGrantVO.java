package com.illunex.emsaasrestapi.partnership.vo;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.time.ZonedDateTime;

@Getter
@Setter
@Alias("PartnershipMemberProductGrantVO")
public class PartnershipMemberProductGrantVO {
    private Integer idx;
    private Integer partnershipMemberIdx;
    private String productCode;
    private String permissionCode;
    private ZonedDateTime createDate;
    private ZonedDateTime updateDate;
}


