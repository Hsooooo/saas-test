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
@Table(name = "finance_stock_analysis")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FinanceStockAnalysis {
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

    /* 주가 분석 */
    private Float per;
    private Float pbr;
    private Float eps;
    private Float evEbitda;
    private Float psr;
    private Float pcr;
    private Float peg;
    private Float dividendYield;                        // 배당수익률
    private Integer bps;
    private Integer sps;
    private Integer cfps;
    private Long marketCap;                            // 시가총액
    private Long shareCapital;                         // 자본금(억단위)
    private Float eva;
    private LocalDateTime createDate;
}
