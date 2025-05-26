package com.illunex.emsaasrestapi.project.document.excel;

import lombok.*;
import org.hibernate.annotations.Comment;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "excel_sheet")
public class ExcelSheet {
    @Comment("엑셀시트명")
    private String excelSheetName;
    @Comment("엑셀파일경로")
    private String filePath;
    @Comment("Cell 목록")
    private List<String> excelCellList;
    @Comment("Row 개수")
    private Integer totalRowCnt;
}
