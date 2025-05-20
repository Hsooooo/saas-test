package com.illunex.emsaasrestapi.project.dto;

import lombok.*;

import java.util.List;

public class RequestProjectCategoryDTO {

    /**
     * 프로젝트 카테고리 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectCategory {
        private Integer idx;
        private String name;
        private Integer sort;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectCategoryModify {
        private Integer partnershipIdx;                     // 파트너쉽 번호
        private List<ProjectCategory> projectCategoryList;  // 카테고리 리스트
        private List<Integer> deleteCategoryIdxList;            // 삭제할 프로젝트 카테고리 idx 리스트
    }
}
