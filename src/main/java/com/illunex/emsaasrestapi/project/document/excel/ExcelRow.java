package com.illunex.emsaasrestapi.project.document.excel;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.LinkedHashMap;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "excel_row")
public class ExcelRow {
    @Id
    private ExcelRowId excelRowId;
    // Row 데이터 정보
    private LinkedHashMap<String, Object> excelRow;
}
