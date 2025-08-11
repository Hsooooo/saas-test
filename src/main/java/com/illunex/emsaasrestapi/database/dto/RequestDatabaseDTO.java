package com.illunex.emsaasrestapi.database.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.List;

public class RequestDatabaseDTO {

    /**
     * Document 타입
     */
    public enum DocType {
        Node,
        Link
    }

    /**
     * 필터 조건
     */
    public enum FilterCondition {
        EQUALS,
        NOT_EQUALS,
        LESS_THAN,
        LESS_THAN_OR_EQUAL,
        GREATER_THAN,
        GREATER_THAN_OR_EQUAL,
        IS,
        IS_NOT,
        EMPTY,
        NOT_EMPTY,
        CONTAINS,
        NOT_CONTAINS
    }
    /**
     * 검색 요청 DTO
     */
    @Getter
    public static class Search {
        private DocType docType;
        private String docName;
        private List<SearchFilter> filters; // 검색할 컬럼 이름들
        private List<SearchSort> sorts;
    }

    /**
     * 조회 조건 DTO
     */
    @Getter
    public static class SearchFilter {
        private String columnName; // 검색할 컬럼 이름
        private FilterCondition filterCondition; // 검색 조건 (예: equals, contains 등)
        private String searchString; // 검색어
    }

    @Getter
    public static class SearchSort {
        private String columnName; // 정렬할 컬럼 이름
        private Boolean isAsc; // 오름차순 여부
    }


    @Getter
    public static class ColumnOrder {
        private Integer projectIdx;
        private String type; // Node 또는 Edge의 타입
        private List<ColumnDetailDTO> columnDetailList; // 컬럼 세부 정보 리스트
    }

    @Getter
    @Setter
    public static class ColumnDetailDTO {
        private String columnName; // 컬럼 이름
        private String alias; // 컬럼 이름(한글)
        private Boolean visible; // 컬럼 표시 여부
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
