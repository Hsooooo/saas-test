package com.illunex.emsaasrestapi.license.vo;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;

/**
 * CREATE TABLE IF NOT EXISTS `em_saas`.`license_partnership` (
 *   `idx` INT(11) NOT NULL AUTO_INCREMENT COMMENT '구독 ID',
 *   `partnership_idx` INT(11) NOT NULL COMMENT '파트너십 ID',
 *   `license_idx` INT(11) NOT NULL COMMENT '현재 적용 라이선스(플랜)',
 *   `billing_day` TINYINT NOT NULL COMMENT '매월 결제 anchor(1~28 권장)',
 *   `period_start_date` DATE NOT NULL COMMENT '현재 청구주기 시작(포함, inclusive)',
 *   `period_end_date` DATE NOT NULL COMMENT '현재 청구주기 종료(배타, exclusive)',
 *   `next_billing_date` DATE NOT NULL COMMENT '다음 결제 예정일(= period_end_date)',
 *   `current_seat_count` INT(11) NOT NULL COMMENT '현재 과금 기준 좌석 수',
 *   `current_unit_price` DECIMAL(12,2) NOT NULL COMMENT '현재 단가 스냅샷(license.price_per_user)',
 *   `current_min_user_count` INT(11) NOT NULL COMMENT '현재 최소유저수 스냅샷(license.min_user_count)',
 *   `cancel_at_period_end` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '다음 결제일에 해지(1) 예약',
 *   `state_cd` VARCHAR(7) NOT NULL COMMENT '파트너쉽 라이센스 상태 코드 (LPS0000)' COLLATE 'utf8mb4_general_ci',
 *   `active_uniquer` INT
 *     GENERATED ALWAYS AS (
 *         IF(`state_cd` IN ('LPS0002', 'LPS0003'), `partnership_idx`, NULL)
 *     ) STORED COMMENT '활성 구독 유니크 강제용 컬럼',
 *
 *   `update_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 *   `create_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 *
 *   PRIMARY KEY (`idx`) USING BTREE,
 *   INDEX `fk_license_partnership_partnership_idx` (`partnership_idx`) USING BTREE,
 *   INDEX `fk_license_partnership_license_idx` (`license_idx`) USING BTREE,
 *   INDEX `license_partnership_next_billing_date` (`next_billing_date`) USING BTREE,
 *   UNIQUE KEY `ui_license_partnership_active_uniquer` (`active_uniquer`),
 *   CONSTRAINT `fk_license_partnership_partnership_idx` FOREIGN KEY (`partnership_idx`) REFERENCES `partnership`(`idx`) ON UPDATE NO ACTION ON DELETE NO ACTION,
 *   CONSTRAINT `fk_license_partnership_license_idx` FOREIGN KEY (`license_idx`) REFERENCES `license`(`idx`) ON UPDATE NO ACTION ON DELETE NO ACTION
 * )
 * COMMENT='파트너쉽 라이센스 정보'
 * COLLATE='utf8mb4_general_ci'
 * ENGINE=InnoDB;
 */
@Setter
@Getter
@Alias("LicensePartnershipVO")
public class LicensePartnershipVO {
    private Integer idx;
    private Integer partnershipIdx;
    private Integer licenseIdx;
    private Integer billingDay;
    private LocalDate periodStartDate;
    private LocalDate periodEndDate;
    private LocalDate nextBillingDate;
    private Integer currentSeatCount;
    private BigDecimal currentUnitPrice;
    private Integer currentMinUserCount;
    private Boolean cancelAtPeriodEnd;
    private String stateCd;
    private ZonedDateTime updateDate;
    private ZonedDateTime createDate;
}
