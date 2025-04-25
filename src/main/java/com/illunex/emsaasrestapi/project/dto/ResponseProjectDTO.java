package com.illunex.emsaasrestapi.project.dto;

import com.illunex.emsaasrestapi.partnership.dto.ResponsePartnershipDTO;
import lombok.*;

import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.List;

public class ResponseProjectDTO {

    /**
     * 프로젝트 설정 요청 구조
     */
    @Getter
    @Setter
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
    public static class ProjectNode {
        private String nodeType;
        private String fieldName;
        private String fieldType;
    }

    /**
     * 엣지 정보
     */
    @Getter
    @Setter
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
    public static class ProjectItem {
        private String label;
        private String nodeType;
        private String fieldName;
        private String fieldType;
        private List<ProjectItemModel> modelList;
    }

    /**
     * 노드 사이즈 & 필터 아이템 범례 정보
     */
    @Getter
    @Setter
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
    public static class ProjectAttributeField {
        private String label;
        private String fieldName;
        private String fieldType;
    }

    /**
     * 프로젝트 엑셀 데이터 정보
     */
    @Getter
    @Setter
    public static class Excel {
        private Integer projectIdx;
        private List<ExcelSheet> excelSheetList;
    }

    /**
     * 프로젝트 엑셀 시트 정보
     */
    @Getter
    @Setter
    public static class ExcelSheet {
        private Integer excelSheetIdx;
        private String excelSheetName;
        // Cell 목록
        private List<String> excelCellList;
        // 엑셀 Row 목록
        private List<ExcelRow> excelRowList;
    }

    /**
     * 프로젝트 엑셀 데이터 키
     */
    @Getter
    @Setter
    public static class ExcelRowId {
        private Integer projectIdx;
        private Integer excelSheetIdx;
        private Integer excelRowIdx;
    }

    /**
     * 프로젝트 엑셀 데이터 정보
     */
    @Getter
    @Setter
    public static class ExcelRow {
        private ExcelRowId excelRowId;
        // Row 데이터 정보
        private LinkedHashMap<String, Object> excelRow;
    }

    /**
     * 카테고리 조회
     */
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProjectCategory {
        private Integer idx;        // 카테고리idx
        private String name;        // 카테고리명
        private Integer projectCnt; // 프로젝트 숫자
        private Integer sort;       // 정렬순서
        ZonedDateTime updateDate;   // 수정일
        ZonedDateTime createDate;   // 생성일
    }


    /**
     * 카테고리의 프로젝트들 조회
     */
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProjectPreview {
        Integer partnershipIdx;     // 파트너쉽idx
        Integer categoryIdx;        // 카테고리idx
        Integer projectIdx;         // 프로젝트idx
        String title;               // 프로젝트명
        Integer nodeCnt;            // 노드개수
        Integer edgeCnt;            // 엣지개수
        String imageUrl;            // 이미지url
        String imagePath;           // 이미지 경로
        ZonedDateTime createDate;   // 생성일
        ZonedDateTime updateDate;   // 수정일
        String statusCd;            // 상태
        List<ResponsePartnershipDTO.MemberPreview> member;  // 프로젝트 구성원
    }
}
