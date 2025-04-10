package com.illunex.emsaasrestapi.partnership.vo;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.time.ZonedDateTime;

@Getter
@Setter
@Alias("PartnershipTeamVO")
public class PartnershipTeamVO {
    private Integer idx;
    private Integer partnershipIdx;
    private String name;
    private String teamImageUrl;
    private String teamImagePath;
    private ZonedDateTime updateDate;
    private ZonedDateTime createDate;
}
