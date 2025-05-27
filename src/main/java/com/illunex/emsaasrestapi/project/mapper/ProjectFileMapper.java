package com.illunex.emsaasrestapi.project.mapper;

import com.illunex.emsaasrestapi.project.vo.ProjectFileVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ProjectFileMapper {
    Integer insertByProjectFileVO(ProjectFileVO projectFileVO);
    List<ProjectFileVO> selectAllByProjectIdx(Integer projectIdx);
    Integer deleteAllByProjectIdx(Integer projectIdx);
}
