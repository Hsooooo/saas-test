package com.illunex.emsaasrestapi.project.document.excel;

import lombok.*;
import org.hibernate.annotations.Comment;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "excel")
public class Excel {
    @Id
    private Integer projectIdx;
    @Comment("엑셀시트 목록")
    private List<ExcelSheet> excelSheetList;
    @Comment("엑셀파일 목록")
    private List<ExcelFile> excelFileList;
    @Comment("등록일")
    private LocalDateTime createDate;
}
