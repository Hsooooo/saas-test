package com.illunex.emsaasrestapi.project.dto;

import com.illunex.emsaasrestapi.common.code.EnumCode;
import com.illunex.emsaasrestapi.member.dto.ResponseMemberDTO;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
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
        private String statusCdDesc;
        private String imageUrl;                                    // 프로젝트 이미지 URL
        private String imagePath;                                   // 프로젝트 이미지 경로
        private Integer nodeCnt;                                    // 노드 개수
        private Integer edgeCnt;                                    // 엣지 개수
        private List<ProjectNode> projectNodeList;                  // 노드정보
        private List<ProjectEdge> projectEdgeList;                  // 엣지정보
        private List<ProjectNodeSize> projectNodeSizeList;          // 노드사이즈정보
        private List<ProjectFilter> projectFilterList;              // 필터링정보
        private List<ProjectNodeContent> projectNodeContentList;    // 속성정보
        private List<ProjectFile> projectFileList;                  // 업로드 파일
        private Excel projectExcel;
        private ZonedDateTime updateDate;
        private ZonedDateTime createDate;
        private ZonedDateTime deleteDate;

        public void setStatusCd(String statusCd) {
            this.statusCd = statusCd;
            this.statusCdDesc = EnumCode.getCodeDesc(statusCd);
        }
    }

    @Getter
    @Setter
    /**
     * 프로젝트 업로드 파일 정보
     */
    public static class ProjectFile {
        private Integer idx;
        private Integer projectIdx;
        private String fileName;
        private String fileUrl;
        private String filePath;
        private String fileSize;
        private String fileCd;
        private String fileCdDesc;
        private ZonedDateTime updateDate;
        private ZonedDateTime createDate;

        public void setFileCd(String fileCd) {
            this.fileCd = fileCd;
            this.fileCdDesc = EnumCode.getCodeDesc(fileCd);
        }
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
    }


    /**
     * 프로젝트 목록 응답 구조
     */
    @Getter
    @Setter
    public static class ProjectListItem {
        private Integer idx;                    // 프로젝트번호
        private Integer partnershipIdx;
        private Integer projectCategoryIdx;
        private String title;
        private String description;
        private String statusCd;
        private String imageUrl;
        private String imagePath;
        private Integer nodeCnt;
        private Integer edgeCnt;
        private ZonedDateTime updateDate;
        private ZonedDateTime createDate;
        private List<ResponseMemberDTO.Member> members;  // 프로젝트 구성원
    }

    /**
     * 엑셀 컬럼 요약 조회(범위값) 응답
     */
    @Getter
    @Setter
    public static class ExcelValueRange {
        private Object min;
        private Object max;
    }

    /**
     * 엑셀 컬럼 요약 조회(선택값) 응답
     */
    @Getter
    @Setter
    public static class ExcelValueDistinct {
        private List<ExcelValueDistinctItem> list;
    }

    @Getter
    @Setter
    public static class ExcelValueDistinctItem {
        private String value;
        private Integer count;
    }

}
