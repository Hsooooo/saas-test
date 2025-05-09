package com.illunex.emsaasrestapi.network.dto;

import com.illunex.emsaasrestapi.project.document.network.Edge;
import com.illunex.emsaasrestapi.project.document.network.Node;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

public class ResponseNetworkDTO {
    /**
     * 관계망 응답 구조
     */
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    public static class Network {
        Integer nodeSize;
        Integer linkSize;
        List<Node> nodes;
        List<Edge> links;

        public Network() {
            if(nodes == null) nodes = new ArrayList<>();
            if(links == null) links = new ArrayList<>();
        }
    }

    /**
     * 관계망 노드 상세 조회
     */
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class NodeDetailInfo {
        String label;
        String cellName;
        String cellType;
        Object value;
    }
}
