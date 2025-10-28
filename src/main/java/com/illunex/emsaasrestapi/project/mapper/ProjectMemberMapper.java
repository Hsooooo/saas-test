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
    int deleteByProjectIdxAndProjectMemberIdxList(Integer projectIdx, List<Integer> projectMemberIdxList);
    Optional<ProjectMemberVO> selectByIdx(Integer idx);

    void updateTypeByIdx(Integer idx, String typeCd);

    boolean existsSoftDeleted(Integer projectIdx, Integer partnershipMemberIdx);

    void undeleteByProjectAndPartnership(Integer projectIdx, Integer partnershipMemberIdx, String typeCd);

    void softDeleteByProjectIdxAndProjectMemberIdxList(Integer projectIdx, List<Integer> projectMemberIdxList);

    void updatePartnershipMemberIdxByPartnershipIdxAndPartnershipMemberIdx(Integer partnershipMemberIdx, Integer transferMemberIdx);
}
