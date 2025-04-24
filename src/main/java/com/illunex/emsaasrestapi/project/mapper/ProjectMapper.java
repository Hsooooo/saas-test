package com.illunex.emsaasrestapi.project.mapper;

import com.illunex.emsaasrestapi.project.dto.RequestProjectDTO;
import com.illunex.emsaasrestapi.project.vo.ProjectVO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ProjectMapper {
    // 프로젝트 등록
    Integer insertByProjectVO(ProjectVO projectVO);
    // 프로젝트 여부 조회
    ProjectVO selectByProjectCategoryIdxAndProjectIdx(Integer projectCategoryIdx, Integer projectIdx);
    Integer deleteByProjectCategoryIdxAndProjectIdx(Integer projectCategoryIdx, Integer projectIdx);
    Integer countByProjectCategoryIdx(Integer idx);
    Integer deleteByIdx(Integer idx);
    List<ProjectVO> selectAllByProjectCategoryIdx(Integer projectCategoryIdx);
    List<ProjectVO> selectAllByProjectCategoryIdxAndPartnerShipIdx(RequestProjectDTO.SelectProject selectProject);
    Integer updateProjectCategoryIdxByProjectVO(ProjectVO projectVO);
    Optional<ProjectVO> selectByIdx(Integer idx);
}
