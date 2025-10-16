package com.illunex.emsaasrestapi.project.mapper;

import com.illunex.emsaasrestapi.project.vo.ProjectMemberViewVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ProjectMemberViewMapper {
    List<ProjectMemberViewVO> selectAllByProjectIdx(Integer projectIdx);
}
