package com.illunex.emsaasrestapi.project.vo;

import lombok.*;
import org.apache.ibatis.type.Alias;

import java.time.ZonedDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Alias("ProjectVO")
public class CategoryVO {
    private Integer idx;
    private String name;
    private Integer sort;
    private ZonedDateTime updateDate;
    private ZonedDateTime createDate;
}
