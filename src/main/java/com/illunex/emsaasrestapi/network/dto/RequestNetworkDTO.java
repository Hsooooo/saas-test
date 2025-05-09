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
    public static class Extend {
        private Integer projectIdx;
        private String label;
        private Integer nodeIdx;
    }
}
