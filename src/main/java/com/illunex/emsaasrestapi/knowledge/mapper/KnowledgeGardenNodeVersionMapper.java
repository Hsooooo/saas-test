package com.illunex.emsaasrestapi.knowledge.mapper;

import com.illunex.emsaasrestapi.knowledge.vo.KnowledgeGardenNodeVersionVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

@Mapper
public interface KnowledgeGardenNodeVersionMapper {
    Optional<KnowledgeGardenNodeVersionVO> selectByIdx(Integer idx);
    void insertByKnowledgeGardenNodeVersionVO(KnowledgeGardenNodeVersionVO knowledgeGardenNodeVersionVO);
}
