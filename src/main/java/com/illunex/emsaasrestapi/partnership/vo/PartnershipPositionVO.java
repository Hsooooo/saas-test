package com.illunex.emsaasrestapi.partnership.vo;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.time.ZonedDateTime;

@Getter
@Setter
@Alias("PartnershipPositionVO")
public class PartnershipPositionVO {
    private Integer idx;
    private Integer partnershipIdx;
    private String name;
    private Integer sortLevel;
    private ZonedDateTime updateDate;
    private ZonedDateTime createDate;
}
