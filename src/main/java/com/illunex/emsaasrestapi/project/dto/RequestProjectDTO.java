package com.illunex.emsaasrestapi.project.dto;

import lombok.*;
import org.springframework.data.domain.PageRequest;

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
        private ProjectId projectId;            // 프로젝트 번호, 파트너쉽 번호
        private String title;                   // 프로젝트 제목
        private String description;             // 프로젝트 내용
        private List<ProjectNode> projectNodeList;            // 노드정보
        private List<ProjectEdge> projectEdgeList;            // 엣지정보
        private List<ProjectNodeSize> projectNodeSizeList;    // 노드사이즈정보
        private List<ProjectAttribute> projectAttributeList;    // 속성정보
    }

    /**
     * 프로젝트 ID 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectId {
        private Integer projectCategoryIdx;
        private Integer projectIdx;
        private Integer partnershipIdx;
    }

    /**
     * 노드 정보
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectNode {
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
    public static class ProjectEdge {
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
    public static class ProjectNodeSize {
        private String categoryName;
        private List<ProjectItem> projectItemList;
    }

    /**
     * 필터 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectFilter {
        private String categoryName;
        private List<ProjectItem> projectItemList;
    }

    /**
     * 노드 사이즈 & 필터 아이템 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectItem {
        private String label;
        private String nodeType;
        private String fieldName;
        private String fieldType;
        private List<ProjectItemModel> projectItemModelList;
    }

    /**
     * 노드 사이즈 & 필터 아이템 범례 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectItemModel {
        private String label;
        private String color;
        private Integer start;
        private Integer end;
    }

    /**
     * 속성 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectAttribute {
        private String nodeType;
        private String labelTitleFieldName;
        private String labelContentFieldName;
        private List<String> labelKeywordList;
        private String keywordSplitValue;
        private List<ProjectAttributeField> projectAttributeFieldList;
    }

    /**
     * 필드 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectAttributeField {
        private String label;
        private String fieldName;
        private String fieldType;
    }
}
