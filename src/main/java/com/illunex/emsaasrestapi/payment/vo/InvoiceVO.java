package com.illunex.emsaasrestapi.payment.vo;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;

/**
 * CREATE TABLE IF NOT EXISTS `em_saas`.`invoice` (
 *   `idx` INT NOT NULL AUTO_INCREMENT COMMENT '인보이스 번호',
 *   `partnership_idx` INT NOT NULL COMMENT '파트너쉽 번호',
 *   `license_partnership_idx` INT NOT NULL COMMENT '라이센스 파트너쉽 번호',
 *   `period_start` DATE NOT NULL COMMENT '청구기간 시작(포함, inclusive)',
 *   `period_end`   DATE NOT NULL COMMENT '청구기간 종료(배타, exclusive)',
 *   `issue_date`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '발행일',
 *   `due_date`     DATETIME NULL COMMENT '납기일',
 *   `subtotal`     DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '세전 합계',
 *   `tax`          DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '세액',
 *   `total`        DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '총액(=subtotal+tax)',
 *   `status_cd`    VARCHAR(7) NOT NULL COMMENT '인보이스 상태 코드 (ISC0000)' COLLATE 'utf8mb4_general_ci',
 *   `unit_cd`      VARCHAR(7) NOT NULL DEFAULT 'MUC0001' COMMENT '화폐단위 (MUC0000)' COLLATE 'utf8mb4_general_ci',
 *   `license_idx`  INT NULL COMMENT '청구 당시 플랜(라이센스) 번호',
 *   `charge_user_count` INT NOT NULL DEFAULT 0 COMMENT '청구 당시 과금 기준 사용자 수',
 *   `receipt_url`  VARCHAR(500) NULL COMMENT '결제전표' COLLATE 'utf8mb4_general_ci',
 *   `meta` JSON NULL COMMENT '스냅샷(시점의 단가/최소과금/분모기준/플랜버전 등)' COLLATE 'utf8mb4_general_ci',
 *   -- 운영
 *   `create_date`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 *   `update_date`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 *   -- 동일 기간 중 "활성 인보이스" 중복 방지용 생성 컬럼(VOID는 중복 허용)
 *   `active_invoice_uniquer` INT
 *     GENERATED ALWAYS AS (
 *       IF(`status_cd` IN ('ICS0004'), NULL, `license_partnership_idx`)
 *     ) STORED COMMENT '활성 인보이스 유니크 강제용(VOID는 NULL)',
 *
 *   PRIMARY KEY (`idx`),
 *   INDEX `fk_invoice_partnership_idx` (`partnership_idx`) USING BTREE,
 *   INDEX `fk_invoice_license_partnership_idx` (`license_partnership_idx`) USING BTREE,
 *   INDEX `invoice_issue_date` (`issue_date`) USING BTREE,
 *   INDEX `invoice_period_start_period_end` (`period_start`, `period_end`) USING BTREE,
 *   -- 동일 LP + 동일 기간에 활성 인보이스 1건만 허용(VOID는 예외)
 *   UNIQUE KEY `ui_invoice_period_active_invoice_uniquer` (`period_start`, `period_end`, `active_invoice_uniquer`),
 *   CONSTRAINT `fk_invoice_partnership_idx` FOREIGN KEY (`partnership_idx`) REFERENCES `partnership`(`idx`) ON UPDATE NO ACTION ON DELETE NO ACTION,
 *   CONSTRAINT `fk_invoice_license_partnership_idx` FOREIGN KEY (`license_partnership_idx`) REFERENCES `license_partnership`(`idx`) ON UPDATE NO ACTION ON DELETE NO ACTION
 * )
 * ENGINE=InnoDB
 * COLLATE='utf8mb4_general_ci'
 * COMMENT='청구서';
 */
@Getter
@Setter
@Alias("InvoiceVO")
public class InvoiceVO {
    private Integer idx;
    private Integer partnershipIdx;
    private Integer licensePartnershipIdx;
    private String orderNumber;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private ZonedDateTime issueDate;
    private ZonedDateTime dueDate;
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal total;
    private String statusCd;
    private String unitCd;
    private Integer licenseIdx;
    private Integer chargeUserCount;
    private String receiptUrl;
    private String meta;
    private ZonedDateTime createDate;
    private ZonedDateTime updateDate;
}
