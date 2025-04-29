package com.illunex.emsaasrestapi.project.document.excel;

import lombok.*;
import org.hibernate.annotations.Comment;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "excel_row_id")
public class ExcelRowId {
    @Comment("프로젝트 번호")
    private Integer projectIdx;
    @Comment("엑셀시트번호")
    private Integer excelSheetIdx;
    @Comment("엑셀Row번호")
    private Integer excelRowIdx;
}
