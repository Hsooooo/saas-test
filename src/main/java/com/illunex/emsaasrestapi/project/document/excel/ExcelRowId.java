package com.illunex.emsaasrestapi.project.document.excel;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "excel_row_id")
public class ExcelRowId {
    private Integer projectIdx;
    private Integer excelSheetIdx;
    private Integer excelRowIdx;
}
