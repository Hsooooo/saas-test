package com.illunex.emsaasrestapi.project.document.excel;

import lombok.*;
import org.hibernate.annotations.Comment;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "excel_sheet")
public class ExcelSheet {
    @Comment("엑셀시트번호")
    private Integer excelSheetIdx;
    @Comment("엑셀시트명")
    private String excelSheetName;
    @Comment("Cell 목록")
    private List<String> excelCellList;
    @Comment("Row 개수")
    private Integer totalRowCnt;
}
