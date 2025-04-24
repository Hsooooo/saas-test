package com.illunex.emsaasrestapi.projectCategory.mapper;

import com.illunex.emsaasrestapi.projectCategory.vo.ProjectCategoryVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ProjectCategoryMapper {
    Integer insertByProjectCategoryVO(ProjectCategoryVO projectCategoryVO);
    Integer updateByProjectCategoryVO(ProjectCategoryVO projectCategoryVO);
    Integer deleteByIdx(Integer idx);
    List<ProjectCategoryVO> selectAllByPartnershipIdx(Integer partnershipIdx);
}
