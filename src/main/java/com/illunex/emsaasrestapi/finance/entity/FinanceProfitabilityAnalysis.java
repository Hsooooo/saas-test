package com.illunex.emsaasrestapi.finance.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "finance_profitability_analysis")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FinanceProfitabilityAnalysis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;
    // 단축종목코드
    @Column(length = 9)
    private String iscd;
    @Column(length = 2)
    private String financialReportingPeriod; // 결산구분(13: 결산, 14: 반기, 15:분기)
    @Column(length = 1)
    private String consolidated; // 연결구분코드(1: 연결재무, 2: 별도재무)
    private Integer fiscalYearEnd;                        // 결산년월

    /* 수익성 분석 */
    private Float operatingProfitMargin;               /* 영업이익률 (Operating Profit Margin) */
    private Float pretaxIncomeMargin;                  /* 세전계속사업이익률 (Pre-Tax Income Margin) */
    private Float netProfitMargin;                     /* 순이익률 (Net Profit Margin) */
    private Float roa;                                   /* ROA (Return on Assets) */
    private Integer ebitdaMargin;                         /* EBITDA 마진율 (EBITDA Margin) */
    private Float totalAssetTurnover;                  /* 총자산회전율 (Total Asset Turnover) */
    private Float returnOnEquity;                      /* 자기자본 순이익률 (ROE) */
    private Float profitLossReversalRank;              /* 흑자/적자 전환 순위(분기) (Profit/Loss Reversal Rank) */
    private Float retentionRatio;                       /* 유보율 (Retention Ratio) */
    private LocalDate createDate;
}
