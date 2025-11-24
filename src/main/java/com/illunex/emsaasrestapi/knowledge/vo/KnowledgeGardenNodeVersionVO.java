package com.illunex.emsaasrestapi.knowledge.vo;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

/**
 CREATE TABLE IF NOT EXISTS `em_saas`.`knowledge_garden_node_version` (
 `idx` INT(11) NOT NULL AUTO_INCREMENT COMMENT '지식정원 노드 버전 번호',
 `node_idx` INT(11) NOT NULL COMMENT '지식정원 노드 번호',
 `version_no` INT(11) NOT NULL COMMENT '버전 번호 (1부터 시작)',
 `title` VARCHAR(255) NOT NULL COMMENT '제목' COLLATE 'utf8mb4_general_ci',
 `content` LONGTEXT NOT NULL COMMENT '내용' COLLATE 'utf8mb4_general_ci',
 `status_cd` VARCHAR(7) NULL DEFAULT NULL COMMENT '노드 상태 코드' COLLATE 'utf8mb4_general_ci',
 `create_date` DATETIME NULL DEFAULT NULL COMMENT '생성일',
 PRIMARY KEY (`idx`) USING BTREE,
 UNIQUE KEY `ui_knowledge_garden_node_version` (`node_idx`, `version_no`) USING BTREE,
 FULLTEXT INDEX `ft_idx_knowledge_garden_node_version` (`title`, `content`),
 INDEX `fk_knowledge_garden_node_version_node_idx` (`node_idx`) USING BTREE,
 CONSTRAINT `fk_knowledge_garden_node_version_node_idx` FOREIGN KEY (`node_idx`) REFERENCES `knowledge_garden_node` (`idx`) ON UPDATE CASCADE ON DELETE CASCADE
 )
 COMMENT='지식정원 노드 버전 정보'
 COLLATE='utf8mb4_general_ci'
 ENGINE=InnoDB;
 */
@Getter
@Setter
@Alias("KnowledgeGardenNodeVersionVO")
public class KnowledgeGardenNodeVersionVO {
    private Integer idx;
    private Integer nodeIdx;
    private Integer versionNo;
    private String title;
    private String content;
    private String stateCd;
    private String noteStatusCd;
    private String createDate;
}
