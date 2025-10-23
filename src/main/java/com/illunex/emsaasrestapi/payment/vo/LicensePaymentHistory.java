package com.illunex.emsaasrestapi.payment.vo;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.time.ZonedDateTime;

/**
 * CREATE TABLE IF NOT EXISTS `em_saas`.`license_payment_history` (
 *   `idx` INT NOT NULL AUTO_INCREMENT COMMENT '결제 내역 번호',
 *   `invoice_idx` INT NOT NULL COMMENT '인보이스 번호',
 *   `provider_cd` VARCHAR(7) NOT NULL COMMENT 'PG사 코드 (PGP0000)' COLLATE 'utf8mb4_general_ci',
 *   `order_number` VARCHAR(100) NOT NULL COMMENT 'PG 거래 ID(고유)' COLLATE 'utf8mb4_general_ci',
 *   `amount` DECIMAL(12,2) NOT NULL COMMENT '수납 금액(양수)',
 *   `unit_cd` VARCHAR(7) NOT NULL DEFAULT 'MUC0001' COMMENT '화폐단위 (MUC0000)' COLLATE 'utf8mb4_general_ci',
 *   `meta` JSON NULL COMMENT 'PG 원천 응답/영수증 스냅샷(JSON)',
 *   `paid_date` DATETIME NOT NULL COMMENT '결제 완료 시각(수납 시각)',
 *   `create_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
 *
 *   PRIMARY KEY (`idx`),
 *   -- 동일 거래 중복 방지(프로바이더+거래ID)
 *   UNIQUE KEY `ui_license_payment_history_provider_cd_order_number` (`provider_cd`, `order_number`),
 *
 *   INDEX `fk_license_payment_history_invoice_idx` (`invoice_idx`),
 *   INDEX `license_payment_history_paid_date` (`paid_date`),
 *
 *   CONSTRAINT `fk_license_payment_history_invoice_idx` FOREIGN KEY (`invoice_idx`) REFERENCES `invoice`(`idx`) ON UPDATE NO ACTION ON DELETE NO ACTION,
 *
 *   -- 금액 유효성(0 초과)
 *   CONSTRAINT `ck_payment_amount_positive` CHECK (`amount` > 0)
 * )
 * ENGINE=InnoDB
 * COLLATE=utf8mb4_general_ci
 * COMMENT='결제 내역(성공 수납 기록)';
 */
@Getter
@Setter
@Alias("LicensePaymentHistory")
public class LicensePaymentHistory {
    private Integer idx;
    private Integer invoiceIdx;
    private String providerCd;
    private String orderNumber;
    private Double amount;
    private String unitCd;
    private String meta;
    private ZonedDateTime paidDate;
    private ZonedDateTime createDate;
}
