package com.illunex.emsaasrestapi.project.document.excel;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.ZonedDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "excel_file")
public class ExcelFile {
    private Integer idx;
    private Integer projectIdx;;
    private String fileName;
    private String fileUrl;
    private String filePath;
    private Long fileSize;
    private String fileCd;
    private ZonedDateTime updateDate;
    private ZonedDateTime createDate;
}
