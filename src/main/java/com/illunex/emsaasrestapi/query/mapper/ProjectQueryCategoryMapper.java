package com.illunex.emsaasrestapi.query.mapper;

import com.illunex.emsaasrestapi.query.vo.ProjectQueryCategoryVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ProjectQueryCategoryMapper {
    void insertByProjectQueryCategoryVO(ProjectQueryCategoryVO projectQueryCategoryVO);
    Optional<ProjectQueryCategoryVO> selectByIdx(Integer idx);
    void updateByProjectQueryCategoryVO(ProjectQueryCategoryVO projectQueryCategoryVO);
    List<ProjectQueryCategoryVO> selectByProjectIdxAndPartnershipMemberIdx(Integer projectIdx, Integer partnershipMemberIdx);
}
