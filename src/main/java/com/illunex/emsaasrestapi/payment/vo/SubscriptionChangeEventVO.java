package com.illunex.emsaasrestapi.payment.vo;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.time.ZonedDateTime;

/**
 * CREATE TABLE IF NOT EXISTS `em_saas`.`subscription_change_event` (
 *   `idx` INT(11) NOT NULL AUTO_INCREMENT COMMENT '구독 변경 이벤트 번호',
 *   `license_partnership_idx` INT(11) NOT NULL COMMENT '파트너쉽 라이센스 번호',
 *   `occurred_date` DATETIME NOT NULL COMMENT '이벤트 발생 시각',
 *   `type_cd` VARCHAR(7) NOT NULL COMMENT '구독 변경 이벤트 코드 (CET0000)' COLLATE 'utf8mb4_general_ci',
 *   `qty_delta` INT NOT NULL DEFAULT 0 COMMENT '좌석 변화량(+증가/-감소), 플랜 변경 시 0 가능',
 *   `from_license_idx` INT NULL COMMENT '플랜 변경 전',
 *   `to_license_idx` INT NULL COMMENT '플랜 변경 후',
 *   `note` VARCHAR(255) NULL COMMENT '비고(요청자, 사유 등)' COLLATE 'utf8mb4_general_ci',
 *   `meta` JSON NULL COMMENT '요청/계산 스냅샷(옵션)' COLLATE 'utf8mb4_general_ci',
 *   `create_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 *   PRIMARY KEY (`idx`),
 *   INDEX `fk_subscription_change_event_from_license_idx` (`from_license_idx`) USING BTREE,
 *   INDEX `fk_subscription_change_event_to_license_idx` (`to_license_idx`) USING BTREE,
 *   CONSTRAINT `fk_subscription_change_event_license_partnership_idx` FOREIGN KEY (`license_partnership_idx`) REFERENCES `license_partnership`(`idx`) ON UPDATE NO ACTION ON DELETE NO ACTION,
 *   CONSTRAINT `fk_subscription_change_event_from_license_idx` FOREIGN KEY (`from_license_idx`) REFERENCES `license`(`idx`) ON UPDATE NO ACTION ON DELETE NO ACTION,
 *   CONSTRAINT `fk_subscription_change_event_to_license_idx` FOREIGN KEY (`to_license_idx`) REFERENCES `license`(`idx`) ON UPDATE NO ACTION ON DELETE NO ACTION
 * )
 * COMMENT='구독 변경 이벤트 정보'
 * COLLATE='utf8mb4_general_ci'
 * ENGINE=InnoDB;
 */
@Getter
@Setter
@Alias("SubscriptionChangeEventVO")
public class SubscriptionChangeEventVO {
    private Integer idx;
    private Integer licensePartnershipIdx;
    private ZonedDateTime occurredDate;
    private String typeCd;
    private Integer qtyDelta;
    private Integer fromLicenseIdx;
    private Integer toLicenseIdx;
    private String note;
    private String meta;
    private String createDate;
}
