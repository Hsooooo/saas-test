package com.illunex.emsaasrestapi.project.mapper;

import com.illunex.emsaasrestapi.partnership.vo.PartnershipVO;
import com.illunex.emsaasrestapi.project.vo.ProjectMemberVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ProjectMemberMapper {
    Integer insertByProjectMemberVO(ProjectMemberVO projectMemberVO);
    List<ProjectMemberVO> selectAllByProjectIdx(Integer projectIdx);
    Optional<ProjectMemberVO> selectByProjectIdxAndPartnershipMemberIdx(Integer projectIdx, Integer partnershipMemberIdx);
}
