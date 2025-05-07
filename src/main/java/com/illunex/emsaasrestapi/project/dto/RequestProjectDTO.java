package com.illunex.emsaasrestapi.project.dto;

import lombok.*;

import java.util.List;

public class RequestProjectDTO {

    /**
     * 프로젝트 설정 구조
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Project {
        private Integer projectIdx;                                 // 프로젝트 번호
        private Integer partnershipIdx;                             // 파트너쉽 번호
        private Integer projectCategoryIdx;                         // 프로젝트 카테고리번호
        private String title;                                       // 프로젝트 제목
        private String description;                                 // 프로젝트 내용
        private List<ProjectNode> projectNodeList;                  // 노드정보
        private List<ProjectEdge> projectEdgeList;                  // 엣지정보
        private List<ProjectNodeSize> projectNodeSizeList;          // 노드사이즈정보
        private List<ProjectFilter> projectFilterList;              // 필터링정보
        private List<ProjectNodeContent> projectNodeContentList;    // 속성정보
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
        private String uniqueCellName;
        private String labelCellName;
        private String labelCellType;
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
        private String srcEdgeCellName;
        private String srcNodeType;
        private String srcNodeCellName;
        private String destEdgeCellName;
        private String destNodeType;
        private String destNodeCellName;
        private String labelEdgeCellName;
        private String labelEdgeCellType;
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
        private String labelCategory;
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
        private String label;
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
        private String cellName;
        private String cellType;
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
        private String value;
    }

    /**
     * 노드 속성 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectNodeContent {
        private String nodeType;
        private String labelTitleCellName;
        private String labelContentCellName;
        private List<String> labelKeywordCellList;
        private String keywordSplitUnit;
        private List<ProjectNodeContentCell> projectNodeContentCellList;
    }

    /**
     * 노드 속성 셀 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectNodeContentCell {
        private String label;
        private String cellName;
        private String cellType;
    }


    /**
     * 관계망 검색
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectNetwork {
        private Integer projectIdx;
        private String searchType;
        private List<String> keywordList;
        private Integer extendDepth;
    }
}
