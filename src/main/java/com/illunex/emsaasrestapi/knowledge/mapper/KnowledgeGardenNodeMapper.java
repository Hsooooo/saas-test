package com.illunex.emsaasrestapi.knowledge.mapper;

import com.illunex.emsaasrestapi.knowledge.vo.KnowledgeGardenNodeVO;
import com.illunex.emsaasrestapi.knowledge.vo.KnowledgeGardenNodeViewVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Mapper
public interface KnowledgeGardenNodeMapper {
    void insertByKnowledgeGardenNodeVO(KnowledgeGardenNodeVO knowledgeGardenNodeVO);
    Optional<KnowledgeGardenNodeVO> selectByIdx(Integer idx);

    List<KnowledgeGardenNodeViewVO> selectTreeNodes(Integer partnershipMemberIdx, String[] includeTypes, Integer parentNodeIdx, Integer limit);

    List<KnowledgeGardenNodeVO> selectBySearchFolder(Integer partnershipMemberIdx, String typeCd, String searchStr, Integer limit);

    List<KnowledgeGardenNodeVO> selectByPartnershipMemberIdxWithLimit(Integer partnershipMemberIdx, Integer limit);

    List<KnowledgeGardenNodeVO> selectByIdxInAndPartnershipMemberIdx(Set<Integer> neighborNodeIds, Integer partnershipMemberIdx);

    Double selectNextSortOrder(Integer partnershipMemberIdx, Integer parentNodeIdx);

    KnowledgeGardenNodeVO selectPrevByNodeIdx(Integer targetNodeIdx);
    KnowledgeGardenNodeVO selectNextByNodeIdx(Integer targetNodeIdx);

    void updateByKnowledgeGardenNodeVO(KnowledgeGardenNodeVO moving);

    void updateDepth(Integer nodeIdx, int newDepth);

    List<KnowledgeGardenNodeVO> selectChildrenByNodeIdx(Integer nodeIdx);

    List<KnowledgeGardenNodeVO> selectByPmIdxAndSearchStrAndTypeCdInWithLimit(Integer partnershipMemberIdx, String searchStr, String[] includeTypes, Integer limit);

    List<KnowledgeGardenNodeVO> selectLinkedNodeByStartNodeIdxAndTypeCd(Integer startNodeIdx, String typeCd);

    void updateCurrentVersionIdx(Integer nodeIdx, Integer currentVersionIdx);

    void updateStateByNodeIds(Integer partnershipMemberIdx, List<Integer> nodeIds, String stateCd);

    List<KnowledgeGardenNodeVO> selectSubtreeNodes(Integer partnershipMemberIdx, Integer rootNodeIdx);

    List<KnowledgeGardenNodeVO> selectTrashNodes(Integer partnershipMemberIdx, String[] includeTypes, String searchStr, Integer limit);

    List<KnowledgeGardenNodeVO> selectBreadCrumbByNodeIdx(Integer nodeIdx, Integer partnershipMemberIdx);
}
