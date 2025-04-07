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
@Table(name = "finance_other_analysis")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FinanceOtherAnalysis {
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

    /* 기타 재무분석 */
    private Integer revenue;                              /* 매출액 (Revenue) */
    private Integer grossProfit;                         /* 매출총(순영업)이익 (Gross Profit) */
    private Integer operatingIncome;                     /* 영업이익 (Operating Income) */
    private Integer pretaxIncome;                        /* 세전계속사업이익 (Pre-Tax Income) */
    private Integer netIncome;                           /* 순이익 (Net Income) */
    private LocalDateTime createDate;
}
