package com.illunex.emsaasrestapi.project.mapper;

import com.illunex.emsaasrestapi.project.vo.ProjectTableVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ProjectTableMapper {
    void insertByProjectTableVO(ProjectTableVO projectTableVO);
    void deleteAllByProjectIdx(Integer projectIdx);

    List<ProjectTableVO> selectAllByProjectIdx(Integer projectIdx);

    List<ProjectTableVO> selectAllByProjectIdxAndTitle(Integer projectIdx, String searchString);
}
