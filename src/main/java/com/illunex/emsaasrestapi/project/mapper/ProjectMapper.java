package com.illunex.emsaasrestapi.project.mapper;

import com.illunex.emsaasrestapi.project.dto.RequestProjectDTO;
import com.illunex.emsaasrestapi.project.vo.ProjectVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ProjectMapper {
    // 프로젝트 등록
    Integer insertByProjectVO(ProjectVO projectVO);
    // 프로젝트 여부 조회
    ProjectVO selectByProjectCategoryIdxAndProjectIdx(Integer projectCategoryIdx, Integer projectIdx);
    // 프로젝트 삭제 처리(삭제일 업데이트)
    Integer updateByDeleteDate(Integer projectIdx);
    // project_category_idx로 프로젝트 총 개수 조회
    Integer countByProjectCategoryIdx(Integer idx);
    // project_category_idx로 프로젝트 조회
    List<ProjectVO> selectAllByProjectCategoryIdx(Integer projectCategoryIdx);
    // 프로젝트의 category 변경
    Integer updateProjectCategoryIdxByProjectVO(ProjectVO projectVO);
    // idx로 프로젝트 조회
    Optional<ProjectVO> selectByIdx(Integer idx);
    // partnership_idx와 project_category_idx로 프로젝트 조회 (pagination)
    List<ProjectVO> selectAllByProjectId(@Param("projectId") RequestProjectDTO.ProjectId projectId, @Param("pageable") Pageable pageable);
    // partnership_idx와 project_category_idx로 프로젝트 총 개수 조회
    Integer countAllByProjectId(RequestProjectDTO.ProjectId projectId);
}
