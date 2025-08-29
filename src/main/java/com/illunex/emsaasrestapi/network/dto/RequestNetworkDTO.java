package com.illunex.emsaasrestapi.network.dto;

import lombok.*;

import java.util.List;

public class RequestNetworkDTO {

    /**
     * 단일 노드 확장 조회
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SelectNode {
        private Integer projectIdx;
        private String label;
        private Object nodeIdx;
        private Integer depth;
    }

    /**
     * 검색한 다중노드 관계망 조회
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Search {
        private Integer projectIdx;
        private String keyword;
    }


    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExtendSearch {
        private Integer projectIdx;         // 프로젝트 ID
        private List<Object> searchIdxList; // 검색할 노드 인덱스 목록
        private String type;                // 검색할 노드 타입
        private Integer depth;              // 확장 깊이
    }

    /**
     * 자동완성 검색
     */
    @Getter
    public static class AutoCompleteSearch {
        private Integer projectIdx;        // 프로젝트 ID
        private List<String> nodeType;     // 검색할 노트타입
        private String searchKeyword;      // 키워드
        private Integer limit;             // 제한수
    }

    /**
     * 최소 / 최대값 집계 조회
     */
    @Getter
    @Setter
    public static class AggregationMinMax {
        private Integer projectIdx;         // 프로젝트 ID
        private String edgeType;            // 엣지 타입(시트명)
        private String labelEdgeCellName;   // 라벨 표시 엣지 셀명
    }
}
