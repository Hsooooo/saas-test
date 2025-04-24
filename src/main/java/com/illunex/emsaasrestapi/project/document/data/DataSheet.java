package com.illunex.emsaasrestapi.project.document.data;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "data_sheet")
public class DataSheet {
    private Integer sheetIdx;
    private String sheetName;
    // Cell 목록
    private List<String> cellList;
    // Row 개수
    private Integer totalRowCnt;
}
