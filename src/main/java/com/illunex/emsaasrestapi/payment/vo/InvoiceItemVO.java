package com.illunex.emsaasrestapi.payment.vo;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.math.BigDecimal;

/**
 * CREATE TABLE IF NOT EXISTS `em_saas`.`invoice_item` (
 *   `idx` INT(11) NOT NULL AUTO_INCREMENT COMMENT '청구 항목 번호',
 *   `invoice_idx` INT(11) NOT NULL COMMENT '인보이스 번호',
 *   `item_type_cd` VARCHAR(7) NOT NULL COMMENT '청구항목유형 (ITC0000)' COLLATE 'utf8mb4_general_ci',
 *   `description`  VARCHAR(255) NULL COMMENT '항목 설명' COLLATE 'utf8mb4_general_ci',
 *
 *   `quantity`   INT NOT NULL DEFAULT 1 COMMENT '수량(좌석 수 등)',
 *   `unit_price` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '단가 스냅샷',
 *   `days`       INT NULL COMMENT '일할 일수(분자, 일할계산때 사용)',
 *   `amount`     DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '금액(마이너스 허용: CREDIT)',
 *
 *   `related_event_idx` INT(11) NULL COMMENT 'subscription_change_event 참조(월중 이벤트 기반)',
 *   `meta` JSON NULL COMMENT '분모/분자/단가/좌석증감/플랜버전 등 재현 스냅샷',
 *   `create_date`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 *
 *   PRIMARY KEY (`idx`),
 *   INDEX `fk_invoice_item_invoice_idx` (`invoice_idx`) USING BTREE,
 *   INDEX `fk_invoice_item_related_event_idx` (`related_event_idx`) USING BTREE,
 *   CONSTRAINT `fk_invoice_item_invoice_idx` FOREIGN KEY (`invoice_idx`) REFERENCES `invoice`(`idx`) ON UPDATE NO ACTION ON DELETE CASCADE,
 *   CONSTRAINT `fk_invoice_item_related_event_idx` FOREIGN KEY (`related_event_idx`) REFERENCES `subscription_change_event`(`idx`) ON UPDATE NO ACTION ON DELETE SET NULL
 * )
 * ENGINE=InnoDB
 * COLLATE=utf8mb4_general_ci
 * COMMENT='청구 항목';
 */
@Getter
@Setter
@Alias("InvoiceItemVO")
public class InvoiceItemVO {
    private Integer idx;
    private Integer invoiceIdx;
    private String itemTypeCd;
    private String description;
    private Integer quantity;
    private BigDecimal unitPrice;
    private Integer days;
    private BigDecimal amount;
    private Integer relatedEventIdx;
    private String meta;
    private String createDate;
}
