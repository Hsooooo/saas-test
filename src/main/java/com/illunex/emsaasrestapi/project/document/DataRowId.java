package com.illunex.emsaasrestapi.project.document;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "dataPropertyId")
public class DataRowId {
    private Integer projectIdx;
    private String sheetName;
    private Integer rowIdx;
}
