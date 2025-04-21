package com.illunex.emsaasrestapi.project.vo;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.time.ZonedDateTime;

@Getter
@Setter
@Alias("ProjectMemberVO")
public class ProjectMemberVO {
    private Integer idx;
    private Integer partnershipIdx;
    private Integer partnershipMemberIdx;
    private String typeCd;
    private String disableFunctions;
    private ZonedDateTime updateDate;
    private ZonedDateTime createDate;
}
