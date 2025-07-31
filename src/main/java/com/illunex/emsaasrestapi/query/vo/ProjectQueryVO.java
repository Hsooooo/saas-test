package com.illunex.emsaasrestapi.query.vo;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.time.ZonedDateTime;

@Getter
@Setter
@Alias("ProjectQueryVO")
public class ProjectQueryVO {
    private Integer idx;
    private Integer partnershipMemberIdx;
    private Integer projectIdx;
    private Integer projectQueryCategoryIdx;
    private String title;
    private String rawQuery;
    private String typeCd;
    private ZonedDateTime updateDate;
    private ZonedDateTime createDate;
}
