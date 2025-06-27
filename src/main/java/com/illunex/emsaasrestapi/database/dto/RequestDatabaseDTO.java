package com.illunex.emsaasrestapi.database.dto;

import lombok.Getter;

import java.util.List;

public class RequestDatabaseDTO {
    @Getter
    public static class Search {
        private DocType docType;
        private String docName;
    }

    public enum DocType {
        Node,
        Link
    }

    @Getter
    public static class ColumnOrder {
        private Integer projectIdx;
        private String type; // Node 또는 Edge의 타입
        private List<ColumnDetailDTO> columnDetailList; // 컬럼 세부 정보 리스트
    }

    @Getter
    public static class ColumnDetailDTO {
        private String columnName; // 컬럼 이름
        private String columnNameKor; // 컬럼 이름(한글)
        private Boolean isVisible; // 컬럼 표시 여부
        private Integer order; // 컬럼 순서
    }

}
