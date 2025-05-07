package com.illunex.emsaasrestapi.project.dto;

import com.illunex.emsaasrestapi.member.dto.ResponseMemberDTO;
import com.illunex.emsaasrestapi.project.document.network.Edge;
import com.illunex.emsaasrestapi.project.document.network.Node;
import lombok.*;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class ResponseProjectDTO {

    /**
     * 프로젝트 설정 구조
     */
    @Getter
    @Setter
    public static class Project {
        private Integer projectIdx;                                 // 프로젝트 번호
        private Integer partnershipIdx;                             // 파트너쉽 번호
        private Integer projectCategoryIdx;                         // 프로젝트 카테고리번호
        private String title;                                       // 프로젝트 제목
        private String description;                                 // 프로젝트 내용
        private String statusCd;                                    // 프로젝트 상태(code 테이블)
        private String imageUrl;                                    // 프로젝트 이미지 URL
        private String imagePath;                                   // 프로젝트 이미지 경로
        private Integer nodeCnt;                                    // 노드 개수
        private Integer edgeCnt;                                    // 엣지 개수
        private List<ProjectNode> projectNodeList;                  // 노드정보
        private List<ProjectEdge> projectEdgeList;                  // 엣지정보
        private List<ProjectNodeSize> projectNodeSizeList;          // 노드사이즈정보
        private List<ProjectFilter> projectFilterList;              // 필터링정보
        private List<ProjectNodeContent> projectNodeContentList;      // 속성정보
        private ZonedDateTime updateDate;
        private ZonedDateTime createDate;
        private ZonedDateTime deleteDate;
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
        private String uniqueCellName;
        private String labelCellName;
        private String labelCellType;
    }

    /**
     * 엣지 정보
     */
    @Getter
    @Setter
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
    public static class ProjectNodeSize {
        private String labelCategory;
        private List<RequestProjectDTO.ProjectItem> projectItemList;
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
        private List<RequestProjectDTO.ProjectItem> projectItemList;
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
        private List<RequestProjectDTO.ProjectItemModel> projectItemModelList;
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
        private String value;
    }

    /**
     * 노드 속성 정보
     */
    @Getter
    @Setter
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
     * 프로젝트 엑셀 데이터 정보
     */
    @Getter
    @Setter
    public static class Excel {
        private Integer projectIdx;
        private List<ExcelSheet> excelSheetList;
        private LocalDateTime createDate;
    }

    /**
     * 프로젝트 엑셀 시트 정보
     */
    @Getter
    @Setter
    public static class ExcelSheet {
        // 엑셀 시트명
        private String excelSheetName;
        // Cell 목록
        private List<String> excelCellList;
        // 엑셀 Row 목록
        private List<ExcelRow> excelRowList;
        // 총 row 수
        private Integer totalRowCnt;
    }

    /**
     * 프로젝트 엑셀 데이터 키
     */
    @Getter
    @Setter
    public static class ExcelRowId {
        private Integer projectIdx;
        private String excelSheetName;
        private Integer excelRowIdx;
    }

    /**
     * 프로젝트 엑셀 데이터 정보
     */
    @Getter
    @Setter
    public static class ExcelRow {
        private ExcelRowId excelRowId;
        private LinkedHashMap<String, Object> data;
        // Row 데이터 정보
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
        List<ResponseMemberDTO.Member> members;  // 프로젝트 구성원
    }


    /**
     * 프로젝트 관계망 조회
     */
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProjectNetwork {
        Integer nodeSize;
        Integer linkSize;
        List<Node> nodes;
        List<Edge> links;

        public void addEdges(List<Edge> edges) {
            if(this.links == null) {
                this.links = new ArrayList<>();
            }

            if(edges != null){
                this.links.addAll(edges);
            }
        }
    }
}
