package com.illunex.emsaasrestapi.database.dto;

import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.List;

public class RequestDatabaseDTO {
    @Getter
    public static class Search {
        private DocType docType;
        private String docName;
        private String searchString; // 검색어
        private List<String> columnNames; // 검색할 컬럼 이름들
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

    @Getter
    public static class Delete {
        private Object id; // 삭제할 데이터의 ID
    }

    @Getter
    public static class Commit {
        List<LinkedHashMap<String, Object>> newData; // 새로 추가할 데이터 리스트
        List<UpdateData> updateData; // 업데이트할 데이터 리스트
        List<Object> deleteData; // 삭제할 데이터 ID 리스트
    }

    @Getter
    public static class UpdateData {
        private Object id;
        private LinkedHashMap<String, Object> data; // 업데이트할 데이터
    }

}
