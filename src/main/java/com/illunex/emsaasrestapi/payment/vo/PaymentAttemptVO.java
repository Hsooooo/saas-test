package com.illunex.emsaasrestapi.payment.vo;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

/**
 * CREATE TABLE IF NOT EXISTS `em_saas`.`payment_attempt` (
 *   `idx` INT(11) NOT NULL AUTO_INCREMENT COMMENT '결제시도 번호',
 *   `invoice_idx` INT(11) NOT NULL COMMENT '인보이스 번호',
 *   `partnership_idx` INT(11) NOT NULL COMMENT '파트너십 번호',
 *   `provider_cd` VARCHAR(7) NOT NULL COMMENT 'PG사 코드' COLLATE 'utf8mb4_general_ci',
 *   `payment_method_idx` INT NULL COMMENT '사용한 결제수단',
 *   `payment_mandate_idx` INT NULL COMMENT '사용한 정기결제번호',
 *
 *   `attempt_no` INT NOT NULL DEFAULT 1 COMMENT '동일 인보이스 내 시도 순번',
 *   `amount` DECIMAL(12,2) NOT NULL COMMENT '시도 금액',
 *   `unit_cd` VARCHAR(7) NOT NULL DEFAULT 'MUC0001' COMMENT '화폐단위' COLLATE 'utf8mb4_general_ci',
 *   `status_cd` VARCHAR(7) NOT NULL COMMENT '시도 상태(PAS0000)' COLLATE 'utf8mb4_general_ci',
 *
 *   `order_number` VARCHAR(100) NULL COMMENT 'PG 거래ID' COLLATE 'utf8mb4_general_ci',
 *   `failure_code` VARCHAR(50) NULL COMMENT '실패 코드',
 *   `failure_message` VARCHAR(255) NULL COMMENT '실패 메시지' COLLATE 'utf8mb4_general_ci',
 *
 *   `request_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '시도 요청 시각',
 *   `respond_date` DATETIME NULL COMMENT 'PG 응답 시각',
 *   `meta` JSON NULL COMMENT '원천 응답 전문/파라미터/3DS 결과 등',
 *
 *   PRIMARY KEY (`idx`),
 *   INDEX `fk_payment_attempt_invoice_idx` (`invoice_idx`) USING BTREE,
 *   INDEX `fk_payment_attempt_partnership_idx` (`partnership_idx`) USING BTREE,
 *   INDEX `fk_payment_attempt_method_idx` (`payment_method_idx`) USING BTREE,
 *   INDEX `fk_payment_attempt_mandate_idx` (`payment_mandate_idx`) USING BTREE,
 *
 *   CONSTRAINT `fk_payment_attempt_invoice_idx` FOREIGN KEY (`invoice_idx`) REFERENCES `invoice`(`idx`) ON UPDATE NO ACTION ON DELETE NO ACTION,
 *   CONSTRAINT `fk_payment_attempt_partnership_idx` FOREIGN KEY (`partnership_idx`) REFERENCES `partnership`(`idx`) ON UPDATE NO ACTION ON DELETE NO ACTION,
 *   CONSTRAINT `fk_payment_attempt_method_idx` FOREIGN KEY (`payment_method_idx`) REFERENCES `partnership_payment_method`(`idx`) ON UPDATE NO ACTION ON DELETE SET NULL,
 *   CONSTRAINT `fk_payment_attempt_mandate_idx` FOREIGN KEY (`payment_mandate_idx`) REFERENCES `payment_mandate`(`idx`) ON UPDATE NO ACTION ON DELETE SET NULL
 * )
 * ENGINE=InnoDB
 * COLLATE='utf8mb4_general_ci'
 * COMMENT='결제 시도/재시도 로그(성공/실패 포함)';
 */
@Getter
@Setter
@Alias("PaymentAttemptVO")
public class PaymentAttemptVO {
    private Integer idx;
    private Integer invoiceIdx;
    private Integer partnershipIdx;
    private String providerCd;
    private Integer paymentMethodIdx;
    private Integer paymentMandateIdx;
    private Integer attemptNo;
    private BigDecimal amount;
    private String unitCd;
    private String statusCd;
    private String orderNumber;
    private String failureCode;
    private String failureMessage;
    private ZonedDateTime requestDate;
    private ZonedDateTime respondDate;
    private String meta;
}
