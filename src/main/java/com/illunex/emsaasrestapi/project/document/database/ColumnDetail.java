package com.illunex.emsaasrestapi.project.document.database;

import lombok.Data;

@Data
public class ColumnDetail {
    private String columnName; // 컬럼 이름
    private String alias; // 별칭
    private Integer order; // 컬럼 순서
    private boolean isVisible; // 컬럼이 보이는지 여부
}
