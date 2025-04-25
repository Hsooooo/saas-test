package com.illunex.emsaasrestapi.project.document.excel;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

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
    private List<ExcelSheet> excelSheetList;
}
