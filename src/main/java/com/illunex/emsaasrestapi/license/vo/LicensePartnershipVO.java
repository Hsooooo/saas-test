package com.illunex.emsaasrestapi.license.vo;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.time.ZonedDateTime;

@Setter
@Getter
@Alias("LicensePartnershipVO")
public class LicensePartnershipVO {
    private Integer idx;
    private Integer licenseIdx;
    private Integer partnershipIdx;
    private String stateCd;
    private ZonedDateTime startDate;
    private ZonedDateTime endDate;
    private ZonedDateTime pauseDate;
    private ZonedDateTime updateDate;
    private ZonedDateTime createDate;
}
