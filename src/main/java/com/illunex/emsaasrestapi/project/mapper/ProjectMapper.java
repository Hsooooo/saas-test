package com.illunex.emsaasrestapi.project.mapper;

import com.illunex.emsaasrestapi.project.dto.RequestProjectDTO;
import com.illunex.emsaasrestapi.project.vo.ProjectVO;
import org.apache.ibatis.annotations.Mapper;

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
    Integer countByProjectCategoryIdx(Integer idx);
    List<ProjectVO> selectAllByProjectCategoryIdx(Integer projectCategoryIdx);
    List<ProjectVO> selectAllByProjectCategoryIdxAndPartnerShipIdx(RequestProjectDTO.SelectProject selectProject);
    Integer updateProjectCategoryIdxByProjectVO(ProjectVO projectVO);
    Optional<ProjectVO> selectByIdx(Integer idx);
}
