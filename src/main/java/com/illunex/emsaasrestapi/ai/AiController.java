package com.illunex.emsaasrestapi.ai;

import com.illunex.emsaasrestapi.ai.dto.RequestAiDTO;
import com.illunex.emsaasrestapi.common.CustomException;
import com.illunex.emsaasrestapi.common.CustomPageRequest;
import com.illunex.emsaasrestapi.common.CustomResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("ai")
public class AiController {

    private final AiService aiService;

    /**
     * 종목 소개 조회
     */
    @GetMapping("/stock/desc")
    public CustomResponse getCompanyDesc(@RequestParam(name = "stockCode") String stockCode) throws CustomException {
        return aiService.getCompanyDesc(stockCode);
    }

    /**
     * 종목 주가 그래프 데이터 조회
     */
    @GetMapping("/stock/graph")
    public CustomResponse getStockGraph(@RequestParam(name = "stockCode") String stockCode,
                                        @RequestParam(name = "calcRange") Integer calcRange) {
        return aiService.getStockGraph(stockCode, calcRange);
    }

    /**
     * 안정성 혁신성 분석 조회
     */
    @GetMapping("/innovation")
    public CustomResponse getInnovation(@RequestParam(name = "stockCode") String stockCode,
                                        @RequestParam(name = "baseYear") Integer baseYear,
                                        @RequestParam(name = "type") RequestAiDTO.InnovationType type) throws CustomException {
        return aiService.getInnovation(stockCode, baseYear, type);
    }

    /**
     * 유사패턴 종목 목록 및 그래프 조회
     * @return
     */
    @GetMapping("/correlation/list")
    public CustomResponse getCorrelation(@RequestParam(name = "stockCode") String stockCode,
                                         @RequestParam(name = "calcRange") Integer calcRange) {
        return aiService.getCorrelation(stockCode, calcRange);
    }

    /**
     * 한국종합주가 지수, 미국종합주가 지수, 유가 지수, 달러활율 지수 그래프 분석 조회
     */
    @GetMapping("/index/graph")
    public CustomResponse getIntegrationIndexGraph(@RequestParam(name = "stockCode") String stockCode,
                                             @RequestParam(name = "calcRange") Integer calcRange) throws CustomException {
        return aiService.getIntegrationIndexGraph(stockCode, calcRange);
    }

    /**
     * 전자공시, 긍부정 지수 추이 목록 조회
     */
    @GetMapping("/disclosure/list")
    public CustomResponse getDisclosureAnalysisList(@RequestParam(name = "stockCode") String stockCode,
                                                    CustomPageRequest pageRequest,
                                                    String[] sort) {
        return aiService.getDisclosureAnalysisList(pageRequest.of(sort), stockCode);
    }

    /**
     * 전자공시, 긍부정 지수 추이 그래프 조회
     */
    @GetMapping("/disclosure/graph")
    public CustomResponse getDisclosureAnalysisGraph(@RequestParam(name = "stockCode") String stockCode) {
        return aiService.getDisclosureAnalysisGraph(stockCode);
    }

    /**
     * 거래처 목록 조회
     */
    @GetMapping("/trade/list")
    public CustomResponse getTradeList(@RequestParam(name = "stockCode") String stockCode,
                                       CustomPageRequest pageRequest,
                                       String[] sort) {
        return aiService.getTradeList(stockCode, pageRequest.of(sort));
    }
}
