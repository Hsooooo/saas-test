package com.illunex.emsaasrestapi.knowledge.mapper;

import com.illunex.emsaasrestapi.knowledge.vo.KnowledgeGardenLinkVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface KnowledgeGardenLinkMapper {
    void insertByKnowledgeGardenLinkVO(Object knowledgeGardenLinkVO);

    List<KnowledgeGardenLinkVO> selectByNodeIds(List<Integer> nodeIds);
    void deleteByParentAndNodeIdx(Integer parentNodeIdx, Integer nodeIdx);
    void deleteByStartNodeAndType(Integer startNodeIdx, String typeCd);

    void updateStateByNodeIds(List<Integer> nodeIds, String stateCd);
    // 추후 참조,유사도 기준 링크 조회 시 사용
    List<KnowledgeGardenLinkVO> selectGraphLinksByNodeIds(List<Integer> nodeIds);
    // 1) 특정 노드 기준 SIMILAR 링크 전체 삭제
    void deleteSimilarLinksByNodeIdx(Integer nodeIdx);

    // 2) 유사도 관계 재계산 대상 이웃 노트 조회
    List<Integer> selectNeighborNotesForRelation(Integer noteIdx);

    // 3) 두 노트가 공유하는 키워드 개수
    Integer countSharedKeywordsBetweenNotes(Integer noteA, Integer noteB);

    // 4) 두 노트 사이 REF 링크 존재 여부
    Integer countRefLinksBetweenNotes(Integer noteA, Integer noteB);

    // 5) SIMILAR 링크 upsert (무방향: start < end)
    void upsertSimilarLink(Integer startNodeIdx, Integer endNodeIdx, Double weight);
}
