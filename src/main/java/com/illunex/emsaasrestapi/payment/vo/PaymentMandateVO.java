package com.illunex.emsaasrestapi.payment.vo;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.time.ZonedDateTime;

/**
 * CREATE TABLE IF NOT EXISTS `em_saas`.`payment_mandate` (
 *   `idx` INT NOT NULL AUTO_INCREMENT COMMENT '정기결제정보 번호',
 *   `partnership_idx` INT NOT NULL COMMENT '파트너십 ID',
 *   `payment_method_idx` INT NOT NULL COMMENT '연결 결제수단',
 *   `provider_cd` VARCHAR(7) NOT NULL COMMENT 'PG사 코드' COLLATE 'utf8mb4_general_ci',
 *
 *   `mandate_id` VARCHAR(100) NOT NULL COMMENT 'PG 정기결제 ID' COLLATE 'utf8mb4_general_ci',
 *   `status_cd` VARCHAR(7) NOT NULL COMMENT '활성/해지 상태(MDS0000)' COLLATE 'utf8mb4_general_ci',
 *
 *   `agree_date` DATETIME NOT NULL COMMENT '동의(체결) 시각',
 *   `revoke_date` DATETIME NULL COMMENT '철회 시각',
 *   `meta` JSON NULL COMMENT '약관 버전, 동의 IP/UA, 증빙 URL 등 스냅샷',
 *
 *   `update_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 *   `create_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 *
 *   PRIMARY KEY (`idx`),
 *   INDEX `fk_payment_mandate_partnership_idx` (`partnership_idx`) USING BTREE,
 *   INDEX `fk_payment_mandate_payment_method_idx` (`payment_method_idx`) USING BTREE,
 *   INDEX `payment_mandate_mandate_id` (`mandate_id`) USING BTREE,
 *
 *   CONSTRAINT `fk_payment_mandate_partnership_idx` FOREIGN KEY (`partnership_idx`) REFERENCES `partnership`(`idx`) ON UPDATE NO ACTION ON DELETE NO ACTION,
 *   CONSTRAINT `fk_payment_mandate_payment_method_idx` FOREIGN KEY (`payment_method_idx`) REFERENCES `partnership_payment_method`(`idx`) ON UPDATE NO ACTION ON DELETE NO ACTION
 * )
 * ENGINE=InnoDB
 * COMMENT='정기결제 위임/동의';
 */
@Getter
@Setter
@Alias("PaymentMandateVO")
public class PaymentMandateVO {
    private Integer idx;
    private Integer partnershipIdx;
    private Integer paymentMethodIdx;
    private String providerCd;
    private String mandateId;
    private String statusCd;
    private ZonedDateTime agreeDate;
    private ZonedDateTime revokeDate;
    private String meta;
    private ZonedDateTime updateDate;
    private ZonedDateTime createDate;
}
