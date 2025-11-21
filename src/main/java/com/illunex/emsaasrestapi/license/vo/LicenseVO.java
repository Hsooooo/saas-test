package com.illunex.emsaasrestapi.license.vo;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

/**
 * CREATE TABLE IF NOT EXISTS `em_saas`.`license` (
 *     `idx` INT(11) NOT NULL AUTO_INCREMENT COMMENT '라이센스 번호',
 *     `plan_cd` VARCHAR(30) NOT NULL COMMENT '플랜 코드 (BASIC, ADVANCED, PREMIUM)',
 *     `name` VARCHAR(50) NULL DEFAULT NULL COMMENT '라이센스 명' COLLATE 'utf8mb4_general_ci',
 *     `description` VARCHAR(255) NULL DEFAULT NULL COMMENT '라이센스 설명' COLLATE 'utf8mb4_general_ci',
 *     `price_per_user` DECIMAL(12, 2) NULL DEFAULT NULL COMMENT '사용자당 가격',
 *     `min_user_count` INT(11) NULL DEFAULT NULL COMMENT '최소 사용자 수',
 *     `data_total_limit` INT(11) NULL DEFAULT NULL COMMENT '데이터 총량 제한(ROW 수)',
 *     `project_count_limit` INT(11) NULL DEFAULT NULL COMMENT '프로젝트 개수 제한',
 *     `period_month` INT(11) NULL DEFAULT 1 COMMENT '결제 주기 개월수 (기본 1개월)',
 *     `version_no` INT(11) DEFAULT 1 COMMENT '요금제 버전',
 *     `partnership_idx` INT(11) NULL DEFAULT NULL COMMENT '파트너쉽 번호',
 *     `active` TINYINT(1) NULL DEFAULT 1 COMMENT '활성화 여부(1:활성, 0:비활성)',
 *     `update_date` DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
 *     `create_date` DATETIME NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
 *     INDEX `license_plan_cd` (`plan_cd`) USING BTREE,
 *     PRIMARY KEY (`idx`) USING BTREE
 * )
 * COMMENT='라이센스 정보'
 * COLLATE='utf8mb4_general_ci'
 * ENGINE=InnoDB;
 */
@Getter
@Setter
@Alias("LicenseVO")
public class LicenseVO {
    private Integer idx;
    private String planCd;
    private String name;
    private String description;
    private BigDecimal pricePerUser;
    private Integer minUserCount;
    private Integer dataTotalLimit;
    private Integer projectCountLimit;
    private Integer periodMonth;
    private Integer versionNo;
    private Boolean active;
    private Integer partnershipIdx;
    private ZonedDateTime updateDate;
    private ZonedDateTime createDate;
}
