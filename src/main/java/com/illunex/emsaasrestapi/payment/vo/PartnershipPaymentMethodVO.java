package com.illunex.emsaasrestapi.payment.vo;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.time.ZonedDateTime;

/**
 * CREATE TABLE IF NOT EXISTS `em_saas`.`partnership_payment_method` (
 *   `idx` INT(11) NOT NULL AUTO_INCREMENT COMMENT '결제수단 번호',
 *   `partnership_idx` INT(11) NOT NULL COMMENT '파트너십 번호',
 *   `method_type_cd` VARCHAR(7) NOT NULL COMMENT '결제수단 유형 (PMC0000: CARD/BANK 등)' COLLATE 'utf8mb4_general_ci',
 *   `brand` VARCHAR(7) NULL COMMENT '카드 브랜드 (CBC0000: VISA/MASTER 등)' COLLATE 'utf8mb4_general_ci',
 *   `last4` VARCHAR(4) NULL COMMENT '카드 끝 4자리' COLLATE 'utf8mb4_general_ci',
 *   `exp_year` VARCHAR(4) NULL COMMENT '유효년(YYYY)' COLLATE 'utf8mb4_general_ci',
 *   `exp_month` VARCHAR(2) NULL COMMENT '유효월(1~12)' COLLATE 'utf8mb4_general_ci',
 *   `holder_name` VARCHAR(100) NULL COLLATE 'utf8mb4_general_ci',
 *
 *   `state_cd` VARCHAR(7) NOT NULL DEFAULT 'PMS0001' COMMENT '수단 상태(PSC0001=ACTIVE, PSC0002=INACTIVE, PSC0003=DELETED 등)' COLLATE 'utf8mb4_general_ci',
 *   `is_default` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '기본 결제수단 여부',
 *
 *   `default_uniquer` INT
 *     GENERATED ALWAYS AS (IF(`is_default`=1 AND `state_cd`='PSC0001', `partnership_idx`, NULL))
 *     STORED COMMENT '활성 기본수단 1건 강제',
 *
 *   `delete_date` DATETIME NULL COMMENT '논리 삭제일',
 *   `update_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 *   `create_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 *
 *   PRIMARY KEY (`idx`),
 *   INDEX `fk_partnership_payment_method_partnership_idx` (`partnership_idx`) USING BTREE,
 *   INDEX `partnership_payment_method_state_cd` (`state_cd`) USING BTREE,
 *   UNIQUE KEY `ui_payment_method_default_per_partnership` (`default_uniquer`),
 *
 *   CONSTRAINT `fk_partnership_payment_method_partnership_idx` FOREIGN KEY (`partnership_idx`) REFERENCES `partnership`(`idx`) ON UPDATE NO ACTION ON DELETE NO ACTION
 * )
 * ENGINE=InnoDB
 * COLLATE='utf8mb4_general_ci'
 * COMMENT='파트너십 결제수단(카드/계좌/토큰)';
 */
@Getter
@Setter
@Alias("PartnershipPaymentMethodVO")
public class PartnershipPaymentMethodVO {
    private Integer idx;
    private Integer partnershipIdx;
    private String methodTypeCd;
    private String brand;
    private String last4;
    private String expYear;
    private String expMonth;
    private String customerKey;
    private String authKey;
    private String holderName;
    private String stateCd;
    private Boolean isDefault;
    private ZonedDateTime deleteDate;
    private ZonedDateTime updateDate;
    private ZonedDateTime createDate;
}
