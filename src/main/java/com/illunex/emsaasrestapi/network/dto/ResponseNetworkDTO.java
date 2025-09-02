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
        private Integer nodeSize;
        private Integer linkSize;
        private List<Node> nodes;
        private List<Edge> links;

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
        private Integer nodeSize;
        private Integer linkSize;
        private List<NodeInfo> nodes;
        private List<EdgeInfo> links;

        public SearchNetwork() {
            this.nodes = new ArrayList<>();
            this.links = new ArrayList<>();
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
        private String label;
        private String cellName;
        private String cellType;
        private Object value;
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
        private NodeId nodeId;
        private Object label;
        private LinkedHashMap<String, Object> properties;

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
        private EdgeId edgeId;
        private Object startType;
        private Object start;
        private Object endType;
        private Object end;
        private LinkedHashMap<String, Object> properties;

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

    /**
     * 자동검색 조회
     */
    @Getter
    @Setter
    @Builder
    public static class AutoComplete {
        private Object nodeId;
        private String nodeLabelTitle;
    }

    /**
     * 관계망 엣지 최소/최대값 조회
     */
    @Getter
    @Setter
    @Builder
    public static class AggregationMinMax {
        private Object min;
        private Object max;
    }
}
