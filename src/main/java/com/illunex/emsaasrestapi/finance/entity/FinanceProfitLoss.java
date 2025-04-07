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
@Table(name = "finance_profit_loss")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FinanceProfitLoss {
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

    /* P/L재무항목 */
    private int depreciation;                         /* 감가상각비 (Depreciation) */
    private int interestExpense;                     /* 이자비용 (Interest Expense) */
    private int interestIncome;                      /* 이자수익 (Interest Income) */
    private int equityMethodGain;                   /* 지분법평가이익 (Equity Method Gain) */
    private int equityMethodLoss;                   /* 지분법평가손익 (Equity Method Loss) */
    private LocalDate createDate;
}
