package com.illunex.emsaasrestapi.query.mapper;

import com.illunex.emsaasrestapi.query.vo.ProjectQueryVO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProjectQueryMapper {
    void insertByProjectQueryVO(ProjectQueryVO projectQueryVO);
}
