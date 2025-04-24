package com.illunex.emsaasrestapi.projectCategory.vo;

import lombok.*;
import org.apache.ibatis.type.Alias;

import java.time.ZonedDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Alias("ProjectCategoryVO")
public class ProjectCategoryVO {
    private Integer idx;
    private Integer partnershipIdx;
    private String name;
    private Integer sort;
    private ZonedDateTime updateDate;
    private ZonedDateTime createDate;
}
