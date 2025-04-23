package com.illunex.emsaasrestapi.project.mapper;

import com.illunex.emsaasrestapi.project.vo.ProjectMemberVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ProjectMemberMapper {
    Integer insertByProjectMemberVO(ProjectMemberVO projectMemberVO);
    List<ProjectMemberVO> selectAllByProjectIdx(Integer projectIdx);
}
