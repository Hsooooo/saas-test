package com.illunex.emsaasrestapi.project.mapper;

import com.illunex.emsaasrestapi.project.dto.RequestProjectDTO;
import com.illunex.emsaasrestapi.project.vo.ProjectVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ProjectMapper {
    // 프로젝트 등록
    Integer insertByProjectVO(ProjectVO projectVO);
    Integer updateByProjectVO(ProjectVO projectVO);
    // 프로젝트 삭제 처리(삭제일 업데이트)
    Integer updateByDeleteDate(Integer projectIdx);
    // 카테고리별 파트너쉽 구성원에 포함되어 있는 프로젝트 총 개수 조회
    Integer countAllByProjectCategoryIdxAndPartnershipMemberIdx(Integer projectCategoryIdx, Integer partnershipMemberIdx);
    // 파트너쉽 구성원에 포함되어 있는 프로젝트 총 개수 조회
    Integer countAllByPartnershipMemberIdx(Integer partnershipMemberIdx);
    // project_category_idx로 프로젝트 조회
    List<ProjectVO> selectAllByProjectCategoryIdx(Integer projectCategoryIdx);
    // 프로젝트의 category 변경
    Integer updateProjectCategoryIdxByProjectVO(ProjectVO projectVO);
    // idx로 프로젝트 조회
    Optional<ProjectVO> selectByIdx(Integer idx);
    // partnership_idx와 project_category_idx로 프로젝트 조회 (pagination)
    List<ProjectVO> selectAllByPartnershipIdxAndProjectCategoryIdx(@Param("partnershipIdx") Integer partnershipIdx, @Param("projectCategoryIdx") Integer projectCategoryIdx , @Param("pageable") Pageable pageable);
    // partnership_idx와 project_category_idx로 프로젝트 총 개수 조회
    Integer countAllByPartnershipIdxAndProjectCategoryIdx(@Param("partnershipIdx") Integer partnershipIdx, @Param("projectCategoryIdx") Integer projectCategoryIdx);
}
