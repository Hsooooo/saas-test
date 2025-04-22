package com.illunex.emsaasrestapi.project.mapper;

import com.illunex.emsaasrestapi.project.dto.RequestProjectDTO;
import com.illunex.emsaasrestapi.project.vo.ProjectCategoryVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ProjectCategoryMapper {

    List<ProjectCategoryVO> findByPartnershipIdx(Integer partnershipIdx);
    Integer findMaxSort(Integer partnershipIdx);
    Integer save(ProjectCategoryVO vo);
    Integer update(ProjectCategoryVO vo);
    Optional<ProjectCategoryVO> selectByProjectCategoryIdx(Integer projectCategoryIdx);
    Integer deleteByProjectCategoryIdx(Integer projectCategoryIdx);
    Integer updateSortByProjectCategory(ProjectCategoryVO projectCategory);
}
