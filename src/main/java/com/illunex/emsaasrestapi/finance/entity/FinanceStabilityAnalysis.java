package com.illunex.emsaasrestapi.finance.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "finance_stability_analysis")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FinanceStabilityAnalysis {
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

    /* 안정성 분석 */
    private float debtRatio;                           /* 부채비율 (Debt Ratio) */
    private float financialCostBurdenRate;           /* 금융비용부담율 (Financial Cost Burden Rate) */
    private float shortTermBorrowingRatio;           /* 단기 차입 비율 (Short-Term Borrowing Ratio) */
    private float debtDependency;                      /* 차입금 의존도 (Debt Dependency) */
    private float accountsReceivableToRevenueRatio; /* 매출채권/매출액 비율 (Accounts Receivable to Revenue Ratio) */
    private float inventoryToRevenueRatio;           /* 재고자산/매출액 비율 (Inventory to Revenue Ratio) */
    private float interestCoverageRatio;              /* 이자보상 배율 (Interest Coverage Ratio) */
    private int netDebt;                             /* 순차입금 (Net Debt) */
    private int shortTermDebt;                      /* 단기성 차입금 (Short-Term Debt) */
    private float operatingIncomeToInterestRatio;   /* 영업이익 이자보상 비율 (Operating Income to Interest Coverage Ratio) */
    private LocalDateTime createDate;
}
