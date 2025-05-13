package com.illunex.emsaasrestapi.network.dto;

import com.illunex.emsaasrestapi.project.document.network.Edge;
import com.illunex.emsaasrestapi.project.document.network.EdgeId;
import com.illunex.emsaasrestapi.project.document.network.Node;
import com.illunex.emsaasrestapi.project.document.network.NodeId;
import lombok.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

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
     * 관계망 응답 구조
     */
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    public static class SearchNetwork {
        Integer nodeSize;
        Integer linkSize;
        List<NodeInfo> nodes;
        List<EdgeInfo> links;

        public SearchNetwork() {
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


    /**
     * 간소화된 노드 정보 조회
     */
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class NodeInfo {
        NodeId nodeId;
        Object label;
        String delimiter;
        LinkedHashMap<String, Object> properties;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            NodeInfo node = (NodeInfo) o;
            return Objects.equals(this.nodeId.getNodeIdx(), node.getNodeId().getNodeIdx()) &&
                    Objects.equals(this.label, node.getLabel());
        }

        @Override
        public int hashCode() {
            return Objects.hash(nodeId.getNodeIdx());
        }
    }

    /**
     * 간소화된 엣지 정보조회
     */
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class EdgeInfo {
        EdgeId edgeId;
        Object startType;
        Object start;
        Object endType;
        Object end;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            EdgeInfo edge = (EdgeInfo) o;
            return Objects.equals(this.edgeId.getEdgeIdx(), edge.getEdgeId().getEdgeIdx());
        }

        @Override
        public int hashCode() {
            return Objects.hash(edgeId.getEdgeIdx());
        }
    }
}
