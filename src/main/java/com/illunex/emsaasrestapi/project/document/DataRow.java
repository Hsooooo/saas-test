package com.illunex.emsaasrestapi.project.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.LinkedHashMap;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "dataProperty")
public class DataRow {
    @Id
    private DataRowId dataRowId;
    // Row 데이터 정보
    private LinkedHashMap<String, Object> dataRow;
}
