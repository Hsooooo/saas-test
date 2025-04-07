package com.illunex.emsaasrestapi.finance;

import com.illunex.emsaasrestapi.common.CustomException;
import com.illunex.emsaasrestapi.common.CustomResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("finance")
@Slf4j
public class FinanceController {

    private final FinanceService financeService;
    /**
     * 종목별 재무 차트(매출/영업이익/순이익) 분기/연간
     */
    @GetMapping("")
    public CustomResponse getFinanceChart(@RequestParam(name = "iscd") String iscd,
                                          @RequestParam(name = "code") String code,
                                          @RequestParam(name = "cnt") int cnt) {
        return financeService.getFinanceChart(iscd, code, cnt);
    }

    /**
     * 종목별 투자 지표
     */
    @GetMapping("/investmentMetrics/{iscd}")
    public CustomResponse getInvestmentMetrics(@PathVariable String iscd) throws CustomException {
        return financeService.getInvestmentMetrics(iscd);
    }

    /**
     * 종목별 가치 평가지표 차트
     */
    @GetMapping("/valuationMetrics/{iscd}")
    public CustomResponse getValuationMetrics(@PathVariable String iscd) throws CustomException {
        return financeService.getValuationMetrics(iscd);
    }

    /**
     * 종목별 가치 평가지표 상세
     */
    @GetMapping("/valuationMetricsDetail")
    public CustomResponse getValuationMetricsList(@RequestParam(name = "iscd") String iscd,
                                                  @RequestParam(name = "code") String code,
                                                  @RequestParam(name = "cnt") int cnt) {
        return financeService.getValuationMetricsList(iscd, code, cnt);
    }

    /**
     * 종목별 안정성 차트(부채비율/유동비율)
     */
    @GetMapping("/stability")
    public CustomResponse getStabilityChart(@RequestParam(name = "iscd") String iscd,
                                            @RequestParam(name = "dateCode") String dateCode,
                                            @RequestParam(name = "searchCode") String searchCode,
                                            @RequestParam(name = "cnt") int cnt) throws CustomException {
        return financeService.getStabilityChart(iscd, dateCode, searchCode, cnt);
    }

    /**
     * 종목별 실적 차트(매출/영업이익)
     */
    @GetMapping("/performance")
    public CustomResponse getFinancialPerformance(@RequestParam(name = "iscd") String iscd,
                                                  @RequestParam(name = "code") String code,
                                                  @RequestParam(name = "cnt") int cnt) throws CustomException {
        return financeService.getFinancialPerformance(iscd, code, cnt);
    }

    /**
     * 재무제표 상세보기(손익계산서/재무상태표/현금흐름표)
     * 하나증권에서 받은 API가 없어 불가
     */
    @GetMapping("/financeDetail")
    public CustomResponse getFinanceDetail(@RequestParam(name = "iscd") String iscd,
                                           @RequestParam(name = "code") String code) {
        return financeService.getFinanceDetail(iscd, code);
    }
}
