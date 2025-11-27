package com.illunex.emsaasrestapi.knowledge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

public class ResponseKnowledgeDTO {
    @Setter
    @Getter
    public static class CreateNode {
        private Integer nodeIdx;
        private String label;
        private String typeCd;
        private String typeCdDesc;

    }
    @Setter
    @Getter
    public static class SearchNetwork {
        private List<NodeInfo> nodes;
        private List<EdgeInfo> edges;
        private Integer nodeSize;
        private Integer edgeSize;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @Builder
    public static class NodeInfo {
        private Integer nodeId;
        private String label;
        private String type;
        private Integer depth;
        private Map<String, Object> properties;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @Builder
    public static class EdgeInfo {
        private Integer edgeId;
        private Integer startNodeId;
        private Integer endNodeId;
        private String type;
        private Float weight;
        private Map<String, Object> properties;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @Builder
    public static class NodeBreadCrumb {
        private Integer nodeIdx;
        private Integer parentNodeIdx;
        private String label;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @Builder
    public static class KnowledgeNode {
        private Integer partnershipIdx;
        private Integer nodeIdx;
        private String label;
        private String content;
        private Integer currentVersionIdx;
        private String noteStatusCd;
        private Integer viewCount;
        private List<NodeInfo> keywordNodeList;
        private List<NodeInfo> referenceNodeList;
        private List<NodeBreadCrumb> pathNodeList;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @Builder
    public static class KnowledgeNodeVersion {
        private Integer idx;
        private Integer nodeIdx;
        private Integer versionNo;
        private String label;
        private String content;
        private String noteStatusCd;
        private String createdAt;
        private Boolean isCurrent;
    }
}
