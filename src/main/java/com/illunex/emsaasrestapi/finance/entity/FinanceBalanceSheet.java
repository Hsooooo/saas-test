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
@Table(name = "finance_balance_sheet")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FinanceBalanceSheet {
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

    /* B/S재무항목 */
    private Integer totalAssets;                         /* 자산총계 (Total Assets) */
    private Integer totalEquity;                         /* 자본총계 (Total Equity) */
    private Integer totalLiabilities;                    /* 부채총계 (Total Liabilities) */
    private LocalDate createDate;
}
