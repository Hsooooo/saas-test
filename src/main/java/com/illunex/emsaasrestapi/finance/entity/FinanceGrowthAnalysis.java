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
@Table(name = "finance_growth_analysis")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FinanceGrowthAnalysis {
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
    private int fiscalYearEnd;                        // 결산년월

    /* 성장성 분석 */
    private Float revenueGrowthRate;                  /* 매출액 증가율 (Revenue Growth Rate) */
    private Float operatingProfitGrowthRate;         /* 영업이익 증가율 (Operating Profit Growth Rate) */
    private Float pretaxIncomeGrowthRate;            /* 세전계속사업이익 증가율 (Pre-Tax Income Growth Rate) */
    private Float netIncomeGrowthRate;               /* 순이익 증가율 (Net Income Growth Rate) */
    private Float epsGrowthRate;                      /* EPS 증가율 (Earnings Per Share Growth Rate) */
    private Float ebitdaGrowthRate;                   /* EBITDA 증가율 (EBITDA Growth Rate) */
    private Float capitalChangeRate;                  /* 자본금 증감률 (Capital Change Rate) */
    private Float totalAssetChangeRate;              /* 총자산 증감률 (Total Asset Change Rate) */
    private Float totalEquityChangeRate;             /* 총자본 증감률 (Total Equity Change Rate) */
    private Float totalLiabilitiesChangeRate;        /* 총부채 증감률 (Total Liabilities Change Rate) */
    private Float profitToRevenueRatio;              /* 매출액 이익률 (Profit to Revenue Ratio) */
    private Integer currentAssetsHigh;                  /* 유동자산 상위 (Current Assets High) */
    private Integer fixedAssetsHigh;                    /* 고정자산 상위 (Fixed Assets High) */
    private Integer currentLiabilitiesLow;              /* 유동부채 하위 (Current Liabilities Low) */
    private Integer fixedLiabilitiesLow;                /* 고정부채 하위 (Fixed Liabilities Low) */
    private LocalDate createDate;
}
