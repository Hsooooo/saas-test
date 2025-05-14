package com.illunex.emsaasrestapi.project.vo;

import lombok.*;
import org.apache.ibatis.type.Alias;

import java.time.ZonedDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Alias("ProjectFileVO")
public class ProjectFileVO {
    private Integer idx;
    private Integer projectIdx;;
    private String fileName;
    private String fileUrl;
    private String filePath;
    private Integer fileSize;
    private String fileCd;
    private ZonedDateTime updateDate;
    private ZonedDateTime createDate;
}
