package com.illunex.emsaasrestapi.knowledge.vo;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

@Getter
@Setter
@Alias("KnowledgeGardenNodeViewVO")
public class KnowledgeGardenNodeViewVO {
    private Integer idx;
    private Integer partnershipMemberIdx;
    private String label;
    private String typeCd;
    private Integer parentNodeIdx;
    private Integer sortOrder;
    private Integer depth;
    private Integer currentVersionIdx;
    private Boolean hasChildren;
    private String updateDate;
    private String createDate;
}
