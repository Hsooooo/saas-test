package com.illunex.emsaasrestapi.project.document.excel;

import lombok.*;
import org.hibernate.annotations.Comment;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
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
    @Comment("Row 정보")
    private LinkedHashMap<String, Object> data;
    @Comment("등록일")
    private LocalDateTime createDate;
}
