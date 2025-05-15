package com.illunex.emsaasrestapi.partnership.vo;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.time.ZonedDateTime;

@Getter
@Setter
@Alias("PartnershipAdditionalVO")
public class PartnershipAdditionalVO {
    private Integer idx;
    private Integer partnershipIdx;
    private String attrKey;
    private String attrValue;
    private ZonedDateTime updateDate;
    private ZonedDateTime createDate;
}
