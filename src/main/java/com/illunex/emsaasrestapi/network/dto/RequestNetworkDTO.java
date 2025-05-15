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
        private Integer nodeIdx;
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
}
