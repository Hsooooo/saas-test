package com.illunex.emsaasrestapi.project.document.excel;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "excel_sheet")
public class ExcelSheet {
    private Integer excelSheetIdx;
    private String excelSheetName;
    // Cell 목록
    private List<String> excelCellList;
    // Row 개수
    private Integer totalRowCnt;
}
