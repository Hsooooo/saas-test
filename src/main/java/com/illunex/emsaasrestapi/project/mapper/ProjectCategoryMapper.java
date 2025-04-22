package com.illunex.emsaasrestapi.project.mapper;

import com.illunex.emsaasrestapi.project.vo.ProjectCategoryVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ProjectCategoryMapper {

    List<ProjectCategoryVO> findByPartnershipIdx(Integer partnershipIdx);
    Integer findMaxSort();
    ProjectCategoryVO save(ProjectCategoryVO vo);
    ProjectCategoryVO update(ProjectCategoryVO vo);

import com.illunex.emsaasrestapi.project.dto.RequestProjectDTO;
import com.illunex.emsaasrestapi.project.vo.ProjectCategoryVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

@Mapper
public interface ProjectCategoryMapper {
    Optional<ProjectCategoryVO> selectByProjectCategoryIdx(Integer projectCategoryIdx);
    Integer deleteByProjectCategoryIdx(Integer projectCategoryIdx);
    Integer updateSortByProjectCategory(RequestProjectDTO.ProjectCategory projectCategory);
}
