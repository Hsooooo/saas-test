package com.illunex.emsaasrestapi.project.dto;

import com.illunex.emsaasrestapi.partnership.dto.ResponsePartnershipDTO;
import lombok.*;

import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.List;

public class ResponseProjectCategoryDTO {

    /**
     * 카테고리 조회
     */
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProjectCategory {
        Integer idx;                // 카테고리 Idx
        String name;                // 카테고리명
        Integer sort;               // 정렬순서
        ZonedDateTime updateDate;   // 수정일
        ZonedDateTime createDate;   // 생성일
        Integer projectCnt;         // 프로젝트 개수
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
        Integer categoryIdx;    //카테고리idx
        Integer projectIdx;     //프로젝트idx
        String title;           //프로젝트명
        Integer nodeCnt;        //노드개수
        Integer edgeCnt;        //엣지개수
        String imageUrl;        //이미지url
        String imagePath;       //이미지 경로
        ZonedDateTime createDate;     //생성일
        ZonedDateTime updateDate;     //수정일
        String statusCd;        //상태
        List<ResponsePartnershipDTO.MemberPreview> member;        //프로젝트 구성원
    }
}
