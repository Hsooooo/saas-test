package com.illunex.emsaasrestapi.finance;

import com.illunex.emsaasrestapi.common.CustomException;
import com.illunex.emsaasrestapi.common.CustomResponse;
import com.illunex.emsaasrestapi.common.ErrorCode;
import com.illunex.emsaasrestapi.common.code.EnumCode;
import com.illunex.emsaasrestapi.finance.dto.ResponseFinanceDTO;
import com.illunex.emsaasrestapi.finance.entity.FinanceOtherAnalysis;
import com.illunex.emsaasrestapi.finance.entity.FinanceProfitabilityAnalysis;
import com.illunex.emsaasrestapi.finance.entity.FinanceStabilityAnalysis;
import com.illunex.emsaasrestapi.finance.entity.FinanceStockAnalysis;
import com.illunex.emsaasrestapi.finance.repository.FinanceOtherAnalysisRepository;
import com.illunex.emsaasrestapi.finance.repository.FinanceProfitabilityAnalysisRepository;
import com.illunex.emsaasrestapi.finance.repository.FinanceStabilityAnalysisRepository;
import com.illunex.emsaasrestapi.finance.repository.FinanceStockAnalysisRepository;
import com.illunex.emsaasrestapi.hts.dto.ResponseHtsDTO;
import com.illunex.emsaasrestapi.hts.entity.*;
import com.illunex.emsaasrestapi.hts.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class FinanceService {
    private final JsiseRepository jsiseRepository;
    private final FinanceStockAnalysisRepository financeStockAnalysisRepository;
    private final FinanceProfitabilityAnalysisRepository financeProfitabilityAnalysisRepository;
    private final FinanceOtherAnalysisRepository financeOtherAnalysisRepository;
    private final FinanceStabilityAnalysisRepository financeStabilityAnalysisRepository;
    private final ModelMapper modelMapper;

    /**
     * 종목별 투자 지표
     * @param iscd
     * @return
     */
    public CustomResponse getInvestmentMetrics(String iscd) throws CustomException {
        JSise jSise = jsiseRepository.findTop1ByIscdOrderByBsopDateDescBsopHourDesc(iscd)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMON_EMPTY, ErrorCode.COMMON_EMPTY.getMessage()));
        // 보통 주식은 연결재무로 보기 때문에 연결재무로 조회.(Consolidated = 1)
        FinanceStockAnalysis financeStockAnalysis = financeStockAnalysisRepository.findTop1ByIscdAndConsolidatedOrderByFiscalYearEndDesc(iscd, "1")
                .orElseThrow(() -> new CustomException(ErrorCode.COMMON_EMPTY, ErrorCode.COMMON_EMPTY.getMessage()));

        FinanceProfitabilityAnalysis financeProfitabilityAnalysis = financeProfitabilityAnalysisRepository.findByIscdAndConsolidatedAndFiscalYearEnd(iscd, "1", financeStockAnalysis.getFiscalYearEnd())
                .orElseThrow(() -> new CustomException(ErrorCode.COMMON_EMPTY, ErrorCode.COMMON_EMPTY.getMessage()));

        ResponseHtsDTO.InvestmentIndicators response = modelMapper.map(financeStockAnalysis, ResponseHtsDTO.InvestmentIndicators.class);

        response.setRoe(financeProfitabilityAnalysis.getReturnOnEquity());
        response.setForeignOwnershipRatio(jSise.getFrgnHldnRate());
        response.setMarketCap(jSise.getAvls());

        return CustomResponse.builder()
                .data(response)
                .message(ErrorCode.OK.getMessage())
                .status(ErrorCode.OK.getStatus())
                .build();
    }

    /**
     * 종목별 가치 평가지표 차트
     * @param iscd
     * @return
     */
    public CustomResponse getValuationMetrics(String iscd) throws CustomException {
        FinanceStockAnalysis financeStockAnalysis = financeStockAnalysisRepository.findTop1ByIscdAndConsolidatedOrderByFiscalYearEndDesc(iscd, "1")
                .orElseThrow(() -> new CustomException(ErrorCode.COMMON_EMPTY, ErrorCode.COMMON_EMPTY.getMessage()));
        ResponseFinanceDTO.FinanceStockAnalysis response = modelMapper.map(financeStockAnalysis, ResponseFinanceDTO.FinanceStockAnalysis.class);
        return CustomResponse.builder()
                .data(response)
                .message(ErrorCode.OK.getMessage())
                .status(ErrorCode.OK.getStatus())
                .build();

    }

    /**
     * 종목별 가치 평가지표 상세
     * @param iscd
     * @param code
     * @param cnt
     * @return
     */
    public CustomResponse getValuationMetricsList(String iscd, String code, int cnt) {
        List<FinanceStockAnalysis> list = new ArrayList<>();
        switch (EnumCode.Finance.FinanceQuarterAnnual.codeToEnum(code)) {
            case Quarter:
                // 분기
                list = financeStockAnalysisRepository.findQuarterTopByIscdAndConsolidatedOrderByFiscalYearEndDesc(cnt, iscd, "1");
                break;
            case Annual:
                // 연간
                list = financeStockAnalysisRepository.findAnnualTopByIscdAndConsolidatedOrderByFiscalYearEndDesc(cnt, iscd, "1");
                break;
        }
        List<ResponseFinanceDTO.FinanceStockAnalysis> detail = modelMapper.map(list, new TypeToken<List<ResponseFinanceDTO.FinanceStockAnalysis>>(){}.getType());
        ResponseFinanceDTO.FinanceStockAnalysisDetail response = new ResponseFinanceDTO.FinanceStockAnalysisDetail();
        response.setChartList(detail);
        response.setUnitCd(code);
        return CustomResponse.builder()
                .data(response)
                .message(ErrorCode.OK.getMessage())
                .status(ErrorCode.OK.getStatus())
                .build();
    }

    /**
     * 종목별 재무 차트(매출/영업이익/순이익)
     * @param iscd
     * @param code
     * @return
     */
    public CustomResponse getFinanceChart(String iscd, String code, int cnt) {
        List<ResponseFinanceDTO.FinanceChart> response = new ArrayList<>();
        switch (EnumCode.Finance.FinanceQuarterAnnual.codeToEnum(code)) {
            case Annual:
                // 연간
                List<FinanceOtherAnalysis> annualList = financeOtherAnalysisRepository.findAnnualTopByIscdAndConsolidatedOrderByFiscalYearEndDesc(cnt, iscd, "1");
                response = modelMapper.map(annualList, new TypeToken<List<ResponseFinanceDTO.FinanceChart>>(){}.getType());
                break;
            case Quarter:
                // 분기
                List<FinanceOtherAnalysis> quarterList = financeOtherAnalysisRepository.findQuarterTopByIscdAndConsolidatedOrderByFiscalYearEndDesc(cnt, iscd, "1");
                response = modelMapper.map(quarterList, new TypeToken<List<ResponseFinanceDTO.FinanceChart>>(){}.getType());
                break;
        }

        for(int i = 0; i < response.size(); i++) {
            response.get(i).setUnitCd(code);
        }

        return CustomResponse.builder()
                .data(response)
                .message(ErrorCode.OK.getMessage())
                .status(ErrorCode.OK.getStatus())
                .build();
    }

    /**
     * 종목별 안정성 차트(부채비율/유동비율)
     * @param iscd
     * @param dateCode
     * @param searchCode
     * @param cnt
     * @return
     */
    public CustomResponse getStabilityChart(String iscd, String dateCode, String searchCode, int cnt) {
        List<ResponseFinanceDTO.FinanceStabilityChart> response = new ArrayList<>();
        switch(EnumCode.Finance.FinanceStability.codeToEnum(searchCode)) {
            case DebtRatio:
                List<FinanceStabilityAnalysis> stabilityAnalysisList = new ArrayList<>();
                switch (EnumCode.Finance.FinanceQuarterAnnual.codeToEnum(dateCode)) {
                    case Annual:
                        // 연간
                        // 연간의 경우 해당연도의 제일 마지막 데이터를 들고 와야 한다.
                        stabilityAnalysisList = financeStabilityAnalysisRepository.findAnnualTopByIscdAndConsolidatedOrderByFiscalYearEndDesc(cnt, iscd, "1");
                        response = modelMapper.map(stabilityAnalysisList, new TypeToken<List<ResponseFinanceDTO.FinanceStabilityChart>>(){}.getType());
                        break;
                    case Quarter:
                        // 분기
                        stabilityAnalysisList = financeStabilityAnalysisRepository.findQuarterTopByIscdAndConsolidatedOrderByFiscalYearEndDesc(cnt, iscd, "1");
                        response = modelMapper.map(stabilityAnalysisList, new TypeToken<List<ResponseFinanceDTO.FinanceStabilityChart>>(){}.getType());
                        break;
                }
                for(int i = 0; i < stabilityAnalysisList.size(); i++) {
                    response.get(i).setValue(stabilityAnalysisList.get(i).getDebtRatio());
                }
                break;
            case CurrentRatio:
                // 현재 유동비율 제공받은 API 없음.
                break;
        }


        for(int i = 0; i < response.size(); i++) {
            response.get(i).setDateUnitCd(dateCode);
            response.get(i).setSearchUnitCd(searchCode);
        }

        return CustomResponse.builder()
                .data(response)
                .message(ErrorCode.OK.getMessage())
                .status(ErrorCode.OK.getStatus())
                .build();
    }

    /**
     * 종목별 실적 차트(매출/영업이익)
     * @param iscd
     * @param code
     * @param cnt
     * @return
     */
    public CustomResponse getFinancialPerformance(String iscd, String code, int cnt) {
        List<ResponseFinanceDTO.FinancialPerformanceChart> chartList = new ArrayList<>();
        List<FinanceOtherAnalysis> financeOtherAnalysisList = new ArrayList<>();
        switch (EnumCode.Finance.FinanceQuarterAnnual.codeToEnum(code)) {
            case Quarter:
                financeOtherAnalysisList = financeOtherAnalysisRepository.findQuarterTopByIscdAndConsolidatedOrderByFiscalYearEndDesc(cnt, iscd, "1");
                // 분기
                break;
            case Annual:
                // 연간
                financeOtherAnalysisList = financeOtherAnalysisRepository.findAnnualTopByIscdAndConsolidatedOrderByFiscalYearEndDesc(cnt, iscd, "1");
                break;
        }
        chartList = modelMapper.map(financeOtherAnalysisList, new TypeToken<List<ResponseFinanceDTO.FinancialPerformanceChart>>(){}.getType());

        int totalRevenue = 0; // 총 매출액
        int totalOperatingIncome = 0; // 총 영업이익
        for(int i = 0; i < chartList.size(); i++) {
            totalRevenue += chartList.get(i).getRevenue();
            totalOperatingIncome += chartList.get(i).getOperatingIncome();
            chartList.get(i).setDateUnitCd(code);
        }

        ResponseFinanceDTO.FinancialPerformanceChartList response = new ResponseFinanceDTO.FinancialPerformanceChartList();
        response.setChartList(chartList);
        response.setTotalRevenue(totalRevenue);
        response.setTotalOperatingIncome(totalOperatingIncome);

        return CustomResponse.builder()
                .data(response)
                .message(ErrorCode.OK.getMessage())
                .status(ErrorCode.OK.getStatus())
                .build();
    }

    /**
     * 재무제표 상세보기(손익계산서/재무상태표/현금흐름표)
     * 하나투자증권에서 받은 API가 없어 현재 불가
     * @param iscd
     * @param code
     * @return
     */
    public CustomResponse getFinanceDetail(String iscd, String code) {
        return null;
    }
}
