package com.illunex.emsaasrestapi.project.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.illunex.emsaasrestapi.project.ProjectComponent;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class RequestProjectDTO {

    /**
     * 프로젝트 목록 조회 요청 구조
     */
    @Getter
    @Setter
    public static class SearchProject {
        private Integer partnershipIdx;                             // 파트너쉽 번호
        private ProjectComponent.CategorySearchType searchType;     // 프로젝트 카테고리 타입
        private Integer projectCategoryIdx;                         // 프로젝트 카테고리번호
        private List<String> statusCdList;
    }

    /**
     * 프로젝트 설정 구조
     */
    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Project {
        private Integer projectIdx;                                 // 프로젝트 번호
        private Integer partnershipIdx;                             // 파트너쉽 번호
        private Integer projectCategoryIdx;                         // 프로젝트 카테고리번호
        private String title;                                       // 프로젝트 제목
        private String description;                                 // 프로젝트 내용
        private String imageUrl;                                    // 프로젝트 이미지 URL
        private String imagePath;                                   // 프로젝트 이미지 경로
        private Integer maxNodeSize;                               // 최대 노드사이즈
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
    public static class ProjectId {
        private Integer projectCategoryIdx;
        private Integer projectIdx;
        private Integer partnershipIdx;
    }

    /**
     * 노드 정보
     */
    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
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
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
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
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ProjectNodeSize {
        private String labelCategory;
        private List<ProjectItem> projectItemList;
    }

    /**
     * 필터 정보
     */
    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ProjectFilter {
        private String label;
        private List<ProjectItem> projectItemList;
    }

    /**
     * 노드 사이즈 & 필터 아이템 정보
     */
    @Getter
    @Setter
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
    public static class ProjectItemModel {
        private String label;
        private String color;
        private String start;
        private String end;
        private String value;
    }

    /**
     * 노드 속성 정보
     */
    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
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
    public static class ProjectNodeContentCell {
        private String label;
        private String cellName;
        private String cellType;
    }

    /**
     * 엑셀 컬럼 요약
     */
    @Getter
    @Setter
    public static class ProjectExcelSummary {
        private Integer projectIdx;
        private String type;
        private String excelSheetName;
        private String excelCellName;
    }

    @Getter
    @Setter
    public static class ProjectMemberUpdate {
        private Integer projectIdx;
        private List<ProjectMember> projectMemberList;
        private List<Integer> deleteProjectMemberIdxList;
    }

    @Getter
    @Setter
    public static class ProjectMember {
        // 프로젝트 구성원 번호
        private Integer projectMemberIdx;
        // 파트너쉽 회원 번호
        private Integer partnershipMemberIdx;
        // 프로젝트 권한
        private String typeCd;
    }
}
