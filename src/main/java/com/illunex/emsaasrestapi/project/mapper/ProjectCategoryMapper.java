package com.illunex.emsaasrestapi.project.mapper;

import com.illunex.emsaasrestapi.project.vo.ProjectCategoryVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ProjectCategoryMapper {
    Integer insertByProjectCategoryVO(ProjectCategoryVO projectCategoryVO);
    Integer updateByProjectCategoryVO(ProjectCategoryVO projectCategoryVO);
    Integer deleteByIdx(Integer idx);
    List<ProjectCategoryVO> selectAllByPartnershipIdx(Integer partnershipIdx);
    Optional<ProjectCategoryVO> selectByProjectCategoryIdx(Integer projectCategoryIdx);
}
