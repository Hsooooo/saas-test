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
}
