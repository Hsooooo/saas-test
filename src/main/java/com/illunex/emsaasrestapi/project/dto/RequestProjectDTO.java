package com.illunex.emsaasrestapi.project.dto;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

public class RequestProjectDTO {

    /**
     * 프로젝트 설정 요청 구조
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Project {
        private ProjectId projectId;
        private List<Node> nodeList;
        private List<Edge> edgeList;
    }

    /**
     * 프로젝트 ID 정보
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectId {
        private Long projectIdx;
        private Long partnershipIdx;
    }

    /**
     * 노드 정보
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Node {
        private String nodeType;
        private String fieldName;
        private String fieldType;
    }

    /**
     * 엣지 정보
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Edge {
        private String edgeType;
        private String srcNodeType;
        private String srcFieldName;
        private String destNodeType;
        private String destFieldName;
        private String labelFieldName;
        private String labelFieldType;
        private String unit;
        private String color;
        private Boolean useDirection;
        private Boolean weight;
    }

    /**
     * 노드 사이즈 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class nodeSize {
        private String categoryName;
        private List<Item> itemList;
    }

    /**
     * 노드 사이즈 아이템 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        private String label;
        private String nodeType;
        private String fieldName;
        private String fieldType;
        private List<Model> modelList;
    }

    /**
     * 노드 사이즈 아이템 범례 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Model {
        private String label;
        private String color;
        private Integer start;
        private Integer end;
    }
}
