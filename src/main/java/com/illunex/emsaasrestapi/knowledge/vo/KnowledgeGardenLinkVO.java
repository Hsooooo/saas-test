package com.illunex.emsaasrestapi.knowledge.vo;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

/**
 * CREATE TABLE IF NOT EXISTS `em_saas`.`knowledge_garden_link` (
 *     `idx` INT(11) NOT NULL AUTO_INCREMENT COMMENT '지식정원 링크 번호',
 *     `start_node_idx` INT(11) NOT NULL COMMENT '시작 노드 번호',
 *     `end_node_idx` INT(11) NOT NULL COMMENT '종료 노드 번호',
 *     `type_cd` VARCHAR(7) NOT NULL COMMENT '관계 타입 코드' COLLATE 'utf8mb4_general_ci',
 *     `weight` FLOAT DEFAULT NULL COMMENT '가중치',
 *     `create_date` DATETIME NULL DEFAULT NULL COMMENT '생성일',
 *     PRIMARY KEY (`idx`) USING BTREE,
 *     INDEX `idx_knowledge_garden_link_start_type` (`start_node_idx`, `type_cd`) USING BTREE,
 *     INDEX `idx_knowledge_garden_link_end_type` (`end_node_idx`, `type_cd`) USING BTREE,
 *     UNIQUE KEY `uq_link_start_end_type` (start_node_idx, end_node_idx, type_cd),
 *     CONSTRAINT `fk_knowledge_garden_link_start_node_idx` FOREIGN KEY (`start_node_idx`) REFERENCES `knowledge_garden_node` (`idx`) ON UPDATE CASCADE ON DELETE CASCADE,
 *     CONSTRAINT `fk_knowledge_garden_link_end_node_idx` FOREIGN KEY (`end_node_idx`) REFERENCES `knowledge_garden_node` (`idx`) ON UPDATE CASCADE ON DELETE CASCADE
 * )
 * COMMENT='지식정원 링크 정보'
 * COLLATE='utf8mb4_general_ci'
 * ENGINE=InnoDB;
 */
@Getter
@Setter
@Alias("KnowledgeGardenLinkVO")
public class KnowledgeGardenLinkVO {
    private Integer idx;
    private Integer startNodeIdx;
    private Integer endNodeIdx;
    private String typeCd;
    private String stateCd;
    private Float weight;
    private String createDate;
}
