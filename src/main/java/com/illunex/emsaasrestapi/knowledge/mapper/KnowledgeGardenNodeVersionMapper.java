package com.illunex.emsaasrestapi.knowledge.mapper;

import com.illunex.emsaasrestapi.knowledge.vo.KnowledgeGardenNodeVersionVO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface KnowledgeGardenNodeVersionMapper {
    void insertByKnowledgeGardenNodeVersionVO(KnowledgeGardenNodeVersionVO knowledgeGardenNodeVersionVO);
}
