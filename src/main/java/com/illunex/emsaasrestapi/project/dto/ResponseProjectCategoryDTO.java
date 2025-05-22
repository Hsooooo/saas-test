package com.illunex.emsaasrestapi.project.dto;

import com.illunex.emsaasrestapi.project.ProjectComponent;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

public class ResponseProjectCategoryDTO {

    /**
     * 카테고리 응답 구조
     */
    @Getter
    @Setter
    public static class ProjectCategory {
        private Integer idx;                                        // 카테고리 Idx
        private String name;                                        // 카테고리명
        private Integer sort;                                       // 정렬순서
        private ProjectComponent.CategorySearchType searchType;     // 프로젝트 카테고리 타입
        private ZonedDateTime updateDate;                           // 수정일
        private ZonedDateTime createDate;                           // 생성일
        private Integer projectCnt;                                 // 프로젝트 개수
    }
}
