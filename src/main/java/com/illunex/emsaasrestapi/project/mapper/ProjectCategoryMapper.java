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
    // 파트너쉽번호에 해당하는 모든 카테고리 조회
    Optional<ProjectCategoryVO> selectByProjectCategoryIdxAndPartnershipIdx(Integer projectCategoryIdx, Integer partnershipIdx);
}
