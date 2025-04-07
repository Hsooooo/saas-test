package com.illunex.emsaasrestapi.finance.dto;

import com.illunex.emsaasrestapi.common.code.EnumCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class ResponseFinanceDTO {

    /**
     * 종목별 가치 평가지표 차트
     */
    @Getter
    @Setter
    public static class FinanceStockAnalysis {
        private float per;
        private float pbr;
        private float psr;
        private int fiscalYearEnd; // 결산년월(yyyyMMdd)
    }

    /**
     * 종목별 가치 평가지표 상세
     */
    @Getter
    @Setter
    public static class FinanceStockAnalysisDetail {
        private List<FinanceStockAnalysis> chartList;
        private String unitCd; // 조회 기간 단위(PQA0001: 분기, PQA0002: 연간)
        private String unitCdDesc;
        public void setUnitCd(String code) {
            this.unitCd = code;
            this.unitCdDesc = EnumCode.getCodeDesc(code);
        }
    }

    /**
     * 종목별 재무 차트(매출/영업이익/순이익)
     */
    @Getter
    @Setter
    public static class FinanceChart {
        private String iscd; // 단축종목코드
        private String financialReportingPeriod; // 결산구분(13: 결산, 14: 반기, 15:분기)
        private String consolidated; // 연결구분코드(1: 연결재무, 2: 별도재무)
        private int fiscalYearEnd;                        // 결산년월

        /* 기타 재무분석 */
        private int revenue;                              /* 매출액 (Revenue) */
        private int grossProfit;                         /* 매출총(순영업)이익 (Gross Profit) */
        private int operatingIncome;                     /* 영업이익 (Operating Income) */
        private int pretaxIncome;                        /* 세전계속사업이익 (Pre-Tax Income) */
        private int netIncome;                           /* 순이익 (Net Income) */

        private String unitCd; // 조회 기간 단위(PQA0001: 분기, PQA0002: 연간)
        private String unitCdDesc;
        public void setUnitCd(String code) {
            this.unitCd = code;
            this.unitCdDesc = EnumCode.getCodeDesc(code);
        }
    }

    /**
     * 종목별 안정성 차트(부채비율/유동비율)
     */
    @Getter
    @Setter
    public static class FinanceStabilityChart {
        private String iscd; // 단축종목코드
        private String financialReportingPeriod; // 결산구분(13: 결산, 14: 반기, 15:분기)
        private String consolidated; // 연결구분코드(1: 연결재무, 2: 별도재무)
        private int fiscalYearEnd;                        // 결산년월
        private float value; // 부채비율 또는 유동비율
        private String dateUnitCd; // 조회 기간 단위(PQA0001: 분기, PQA0002: 연간)
        private String dateUnitCdDesc;
        private String searchUnitCd; // 차트 조회 구분(FSA0001: 부채비율, FSA0002: 유동비율) 현재 유동비율은 제공 받은 데이터 없음
        private String searchUnitCdDesc;
        public void setDateUnitCd(String code) {
            this.dateUnitCd = code;
            this.dateUnitCdDesc = EnumCode.getCodeDesc(code);
        }

        public void setSearchUnitCd(String code) {
            this.searchUnitCd = code;
            this.searchUnitCdDesc = EnumCode.getCodeDesc(code);
        }
    }

    /**
     * 종목별 실적 차트(매출/영업이익)
     */
    @Getter
    @Setter
    public static class FinancialPerformanceChartList {
        private List<FinancialPerformanceChart> chartList;
        private int totalRevenue; // 총 매출액
        private int totalOperatingIncome; // 총 영업이익
    }

    @Getter
    @Setter
    public static class FinancialPerformanceChart {
        private String iscd; // 단축종목코드
        private String financialReportingPeriod; // 결산구분(13: 결산, 14: 반기, 15:분기)
        private String consolidated; // 연결구분코드(1: 연결재무, 2: 별도재무)
        private int fiscalYearEnd;                        // 결산년월
        private int revenue; // 매출액
        private int operatingIncome; // 영업이익
        private String dateUnitCd; // 조회 기간 단위(PQA0001: 분기, PQA0002: 연간)
        private String dateUnitCdDesc;
        public void setDateUnitCd(String code) {
            this.dateUnitCd = code;
            this.dateUnitCdDesc = EnumCode.getCodeDesc(code);
        }
    }
}
