package com.illunex.emsaasrestapi.project.mapper;

import com.illunex.emsaasrestapi.project.vo.ProjectVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ProjectMapper {
    Integer insertProjectVO(ProjectVO projectVO);
    Integer countByProjectCategoryIdx(Integer idx);
    Integer deleteByIdx(Integer idx);
    List<ProjectVO> selectAllByProjectCategoryIdx(Integer projectCategoryIdx);
    Integer updateProjectCategoryIdxByProjectVO(ProjectVO projectVO);
}
