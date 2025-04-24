package com.illunex.emsaasrestapi.project.document.data;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "data_row_id")
public class DataRowId {
    private Integer projectIdx;
    private Integer sheetIdx;
    private Integer rowIdx;
}
