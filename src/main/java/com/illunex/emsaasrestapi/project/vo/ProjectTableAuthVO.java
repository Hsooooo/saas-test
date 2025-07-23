package com.illunex.emsaasrestapi.project.vo;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.time.ZonedDateTime;

@Getter
@Setter
@Alias("ProjectTableAuthVO")
public class ProjectTableAuthVO {
    private Integer idx;
    private Integer projectTableIdx;
    private Integer partnershipMemberIdx;
    private String authCd;
    private ZonedDateTime updateDate;
    private ZonedDateTime createDate;
}
