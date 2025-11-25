package com.illunex.emsaasrestapi.query.mapper;

import com.illunex.emsaasrestapi.query.vo.ProjectQueryVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ProjectQueryMapper {
    void insertByProjectQueryVO(ProjectQueryVO projectQueryVO);
    void updateByProjectQueryVO(ProjectQueryVO projectQueryVO);
    Optional<ProjectQueryVO> selectByIdx(Integer idx);
    List<ProjectQueryVO> selectByProjectQueryCategoryIdx(Integer projectQueryCategoryIdx);
    List<ProjectQueryVO> selectByProjectQueryCategoryIdxAndPartnershipMemberIdx(Integer projectQueryCategoryIdx, Integer partnershipMemberIdx);
}
