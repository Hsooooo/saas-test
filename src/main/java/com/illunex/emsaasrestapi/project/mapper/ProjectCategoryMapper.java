package com.illunex.emsaasrestapi.project.mapper;

import com.illunex.emsaasrestapi.project.vo.ProjectCategoryVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ProjectCategoryMapper {

    List<ProjectCategoryVO> findAll();
    List<ProjectCategoryVO> findByPartnershipIdx(Integer idx);
    ProjectCategoryVO save(ProjectCategoryVO vo);
    Integer findMaxSort();

}
