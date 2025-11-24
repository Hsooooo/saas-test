package com.illunex.emsaasrestapi.knowledge.mapper;

import com.illunex.emsaasrestapi.knowledge.vo.KnowledgeGardenNodeVersionVO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Mapper
public interface KnowledgeGardenNodeVersionMapper {
    Optional<KnowledgeGardenNodeVersionVO> selectByIdx(Integer idx);
    void insertByKnowledgeGardenNodeVersionVO(KnowledgeGardenNodeVersionVO knowledgeGardenNodeVersionVO);
    List<KnowledgeGardenNodeVersionVO> selectByNodeIdxWithPageable(Integer nodeIdx, Pageable pageable);
    long countByNodeIdx(Integer nodeIdx);
}
