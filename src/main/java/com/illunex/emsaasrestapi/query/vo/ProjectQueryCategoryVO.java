package com.illunex.emsaasrestapi.query.vo;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.time.ZonedDateTime;

@Getter
@Setter
@Alias("ProjectQueryCategoryVO")
public class ProjectQueryCategoryVO {
    private Integer idx;
    private Integer partnershipMemberIdx;
    private Integer projectIdx;
    private String name;
    private Integer sort;
    private ZonedDateTime updateDate;
    private ZonedDateTime createDate;
}
