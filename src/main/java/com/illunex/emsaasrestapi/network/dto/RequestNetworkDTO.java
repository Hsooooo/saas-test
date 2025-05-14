package com.illunex.emsaasrestapi.network.dto;

import lombok.*;

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
}
