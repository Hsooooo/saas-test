package com.illunex.emsaasrestapi.ai.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Comment;

import java.time.LocalDate;

/**
 * 거래내역
 */
@Getter
@Setter
@Entity
@Table(name = "ai_dart_order_rcept")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiDartOrderRcept {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Comment("법인 이름")
    private String corpName;
    @Comment("dart 고유번호")
    private String corpCode;
    @Comment("단축종목코드")
    private String stockCode;
    @Comment("보고서 이름")
    private String reportNm;
    @Comment("공시번호")
    private String rceptNo;
    @Comment("공시날짜")
    private LocalDate rceptDt;
    @Comment("계약 대상")
    private String targetCompany;
    @Comment("계약 금액")
    private String contractAmount;
    @Comment("매출 대비 계약 금액 비율")
    private String contractAmountRatio;
    @Comment("계약 시작일")
    private String contractStartDate;
    @Comment("계약 종료일")
    private String contractEndDate;
    @Comment("계약기간(단위 : 일)")
    private Float contractRange;
    @Comment("생성일")
    private LocalDate created;
}
