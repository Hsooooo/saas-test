package com.illunex.emsaasrestapi.project.vo;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.time.ZonedDateTime;

@Getter
@Setter
@Alias("ProjectCategoryVO")
public class ProjectCategoryVO {
    private Integer idx;
    private Integer partnershipIdx;
    private String name;
    private Integer sort;
    private ZonedDateTime updateDate;
    private ZonedDateTime createDate;
}
