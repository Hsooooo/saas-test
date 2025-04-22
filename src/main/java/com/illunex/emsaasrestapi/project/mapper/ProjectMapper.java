package com.illunex.emsaasrestapi.project.mapper;

import com.illunex.emsaasrestapi.project.vo.ProjectVO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProjectMapper {
    Integer insertProjectVO(ProjectVO projectVO);
    Integer countByProjectCategoryIdx(Integer idx);
}
