package com.illunex.emsaasrestapi.project.mapper;

import com.illunex.emsaasrestapi.project.vo.ProjectTableAuthVO;
import com.illunex.emsaasrestapi.project.vo.ProjectTableVO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProjectTableAuthMapper {
    void insertByProjectTableAuthVO(ProjectTableAuthVO projectTableAuthVO);
}
