package com.illunex.emsaasrestapi.knowledge.vo;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

/**
 * CREATE TABLE IF NOT EXISTS `em_saas`.`knowledge_garden_node` (
 *      `idx` INT(11) NOT NULL AUTO_INCREMENT COMMENT '지식정원 노드 번호',
 *      `partnership_member_idx` INT(11) NULL DEFAULT NULL COMMENT '파트너십 회원 번호',
 *      `label` VARCHAR(255) NULL DEFAULT NULL COMMENT '라벨' COLLATE 'utf8mb4_general_ci',
 *      `type_cd` VARCHAR(7) NULL DEFAULT NULL COMMENT '노드 타입 코드' COLLATE 'utf8mb4_general_ci',
 *      `parent_node_idx` INT(11) NULL DEFAULT NULL COMMENT '상위 노드 번호',
 *      `sort_order` INT(11) NOT NULL DEFAULT 0 COMMENT '동일 parent_node_idx 내 정렬순서',
 *      `depth` INT(11) NOT NULL DEFAULT 0 COMMENT '트리 깊이 (0=root)',
 *      `current_version_idx` INT(11) NULL DEFAULT NULL COMMENT '현재 버전 노드 번호',
 *      `update_date` DATETIME NULL DEFAULT NULL COMMENT '수정일',
 *      `create_date` DATETIME NULL DEFAULT NULL COMMENT '생성일',
 *      PRIMARY KEY (`idx`) USING BTREE,
 *      INDEX `fk_knowledge_garden_node_partnership_member_idx` (`partnership_member_idx`) USING BTREE,
 *      INDEX `idx_node_parent_sort` (`parent_node_idx`, `sort_order`) USING BTREE,
 *      INDEX `idx_knowledge_garden_node_node_type` (`type_cd`) USING BTREE,
 *      INDEX `idx_current_version_idx` (`current_version_idx`) USING BTREE,
 *      CONSTRAINT `fk_knowledge_garden_node_partnership_member_idx` FOREIGN KEY (`partnership_member_idx`) REFERENCES `partnership_member` (`idx`) ON UPDATE NO ACTION ON DELETE NO ACTION
 * )
 * COMMENT='지식정원 노드 정보'
 * COLLATE='utf8mb4_general_ci'
 * ENGINE=InnoDB;
 */
@Getter
@Setter
@Alias("KnowledgeGardenNodeVO")
public class KnowledgeGardenNodeVO {
    private Integer idx;
    private Integer partnershipMemberIdx;
    private String label;
    private String typeCd;
    private Integer parentNodeIdx;
    private Double sortOrder;
    private Integer depth;
    private Integer currentVersionIdx;
    private String updateDate;
    private String createDate;
}
