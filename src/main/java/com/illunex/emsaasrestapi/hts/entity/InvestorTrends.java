package com.illunex.emsaasrestapi.hts.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "investor_trends")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InvestorTrends {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idx;
    @Column(length = 9)
    private String iscd;                           // 단축종목코드
    @Column(length = 8)
    private String investorDate;                  // 투자자 동향 조회 날
    private Long netForeignBuyingVolume;          // 외국인 순매수(수량)
    private Long foreignBuyVolume;          // 외국인 매수(수량)
    private Long foreignSellVolume;          // 외국인 매도(수량)
    private Long foreignNetBuyAmount; //외국인 순매수(금액)
    private Long foreignBuyAmount; //외국인 매수(금액)
    private Long foreignSellAmount; //외국인 매도(금액)
    private Long individualNetBuyVolume; //개인 순매수(수량)
    private Long individualBuyVolume; //개인매수(수량)
    private Long individualSellVolume; //개인 매도(수량)
    private Long individualNetBuyAmount; // 개인 순매수(금액)
    private Long individualBuyAmount; // 개인 매수(금액)
    private Long individualSellAmount; // 개인 매도(금액)
    private Long institutionalNetBuyVolume; // 기관계 순매수(수량)
    private Long institutionalBuyVolume; // 기관계 매수(수량)
    private Long institutionalSellVolume; // 기관계 매도(수량)
    private Long institutionalNetBuyAmount; // 기관계 순매수(금액)
    private Long institutionalBuyAmount; // 기관계 매수(금액)
    private Long institutionalSellAmount; // 기관계 매도(금액)
    private Long investmentTrustNetBuyVolume; // 투신 순매수(수량)
    private Long investmentTrustBuyVolume; // 투신 매수(수량)
    private Long investmentTrustSellVolume; // 투신 매도(수량)
    private Long investmentTrustNetBuyAmount; // 투신 순매수(금액)
    private Long investmentTrustBuyAmount; // 투신 매수(금액)
    private Long investmentTrustSellAmount; // 투신 매도(금액)
    private Long privateEquityNetBuyVolume; // 사모펀드 순매수(수량)
    private Long privateEquityBuyVolume; // 사모펀드 매수(수량)
    private Long privateEquitySellVolume; // 사모펀드 매도(수량)
    private Long privateEquityNetBuyAmount; // 사모펀드 순매수(금액)
    private Long privateEquityBuyAmount; // 사모펀드 매수(금액)
    private Long privateEquitySellAmount; // 사모펀드 매도(금액)
    private Long financialInvestmentNetBuyVolume; // 금융투자 순매수(수량)
    private Long financialInvestmentBuyVolume; // 금융투자 매수(수량)
    private Long financialInvestmentSellVolume; // 금융투자 매도(수량)
    private Long financialInvestmentNetBuyAmount; // 금융투자 순매수(금액)
    private Long financialInvestmentBuyAmount; // 금융투자 매수(금액)
    private Long financialInvestmentSellAmount; // 금융투자 매도(금액)
    private Long insuranceNetBuyVolume; // 보험 순매수(수량)
    private Long insuranceBuyVolume; // 보험 매수(수량)
    private Long insuranceSellVolume; // 보험 매도(수량)
    private Long insuranceNetBuyAmount; // 보험 순매수(금액)
    private Long insuranceBuyAmount; // 보험 매수(금액)
    private Long insuranceSellAmount; // 보험 매도(금액)
    private Long bankNetBuyVolume; // 은행 순매수(수량)
    private Long bankBuyVolume; // 은행 매수(수량)
    private Long bankSellVolume; // 은행 매도(수량)
    private Long bankNetBuyAmount; // 은행 순매수(금액)
    private Long bankBuyAmount; // 은행 매수(금액)
    private Long bankSellAmount; // 은행 매도(금액)
    private Long otherFinancialNetBuyVolume; // 기타금융 순매수(수량)
    private Long otherFinancialBuyVolume; // 기타금융 매수(수량)
    private Long otherFinancialSellVolume; // 기타금융 매도(수량)
    private Long otherFinancialNetBuyAmount; // 기타금융 순매수(금액)
    private Long otherFinancialBuyAmount; // 기타금융 매수(금액)
    private Long otherFinancialSellAmount; // 기타금융 매도(금액)
    private Long pensionFundsNetBuyVolume; // 연기금 등 순매수(수량)
    private Long pensionFundsBuyVolume; // 연기금 등 매수(수량)
    private Long pensionFundsSellVolume; // 연기금 등 매도(수량)
    private Long pensionFundsNetBuyAmount; // 연기금 등 순매수(금액)
    private Long pensionFundsBuyAmount; // 연기금 등 매수(금액)
    private Long pensionFundsSellAmount; // 연기금 등 매도(금액)
    private Long otherCorporationsNetBuyVolume; // 기타법인 순매수(수량)
    private Long otherCorporationsBuyVolume; // 기타법인 매수(수량)
    private Long otherCorporationsSellVolume; // 기타법인 매도(수량)
    private Long otherCorporationsNetBuyAmount; // 기타법인 순매수(금액)
    private Long otherCorporationsBuyAmount; // 기타법인 매수(금액)
    private Long otherCorporationsSellAmount; // 기타법인 매도(금액)
    private Long domesticAndForeignNetBuyVolume; // 내외국인 순매수(수량)
    private Long domesticAndForeignBuyVolume; // 내외국인 매수(수량)
    private Long domesticAndForeignSellVolume; // 내외국인 매도(수량)
    private Long domesticAndForeignNetBuyAmount; // 내외국인 순매수(금액)
    private Long domesticAndForeignBuyAmount; // 내외국인 매수(금액)
    private Long domesticAndForeignSellAmount; // 내외국인 매도(금액)
    private LocalDate createDate;
    private LocalDate updateDate;
}
