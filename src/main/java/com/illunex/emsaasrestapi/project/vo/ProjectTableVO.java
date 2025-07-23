package com.illunex.emsaasrestapi.project.vo;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.time.ZonedDateTime;

@Getter
@Setter
@Alias("ProjectTableVO")
public class ProjectTableVO {
    private Integer idx;
    private Integer projectIdx;
    private String title;
    private Integer dataCount;
    private String typeCd;
    private ZonedDateTime updateDate;
    private ZonedDateTime createDate;
}
