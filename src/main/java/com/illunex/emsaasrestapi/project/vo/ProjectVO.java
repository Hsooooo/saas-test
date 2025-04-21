package com.illunex.emsaasrestapi.project.vo;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.time.ZonedDateTime;

@Getter
@Setter
@Alias("ProjectVO")
public class ProjectVO {
    private Integer idx;
    private Integer partnershipCategoryIdx;
    private String title;
    private String description;
    private String statusCd;
    private String imageUrl;
    private String imagePath;
    private Integer nodeCnt;
    private Integer edgeCnt;
    private ZonedDateTime updateDate;
    private ZonedDateTime createDate;
}
