package com.illunex.emsaasrestapi.ai;

import com.illunex.emsaasrestapi.ai.dto.RequestAiDTO;
import com.illunex.emsaasrestapi.ai.dto.ResponseAiDTO;
import com.illunex.emsaasrestapi.ai.dto.ResponseAiMapper;
import com.illunex.emsaasrestapi.ai.entity.*;
import com.illunex.emsaasrestapi.ai.repository.*;
import com.illunex.emsaasrestapi.common.CustomException;
import com.illunex.emsaasrestapi.common.CustomResponse;
import com.illunex.emsaasrestapi.common.ErrorCode;
import com.illunex.emsaasrestapi.common.code.EnumCode;
import com.illunex.emsaasrestapi.company.entity.CompanyDesc;
import com.illunex.emsaasrestapi.hts.entity.JSise;
import com.illunex.emsaasrestapi.hts.entity.JStockJong;
import com.illunex.emsaasrestapi.hts.entity.ThemeOriginal;
import com.illunex.emsaasrestapi.company.repository.CompanyDescRepository;
import com.illunex.emsaasrestapi.hts.repository.JsiseRepository;
import com.illunex.emsaasrestapi.hts.repository.JstockJongRepository;
import com.illunex.emsaasrestapi.hts.repository.ThemeOriginalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class AiService {
    private final JstockJongRepository jstockJongRepository;
    private final JsiseRepository jsiseRepository;
    private final ThemeOriginalRepository themeOriginalRepository;
    private final CompanyDescRepository companyDescRepository;
    private final AiFinanceIndexesRepository aiFinanceIndexesRepository;
    private final AiOilIndexRepository aiOilIndexRepository;
    private final AiStockCorrelationRepository aiStockCorrelationRepository;
    private final AiStockIndexCorrelationRepository aiStockIndexCorrelationRepository;
    private final AiStockInnovationScoreRepository aiStockInnovationScoreRepository;
    private final AiStockValuesRepository aiStockValuesRepository;
    private final AiDartAnalysisRepository aiDartAnalysisRepository;
    private final AiDartOrderRceptRepository aiDartOrderRceptRepository;

    private final ModelMapper modelMapper;

    /**
     * 지수 구분
     */
    public enum IndexType {
        kospi,
        kosdaq,
        dji,
        nasdaq;

        public static IndexType stringToEnum(String type) {
            if(type.isEmpty() || type.isBlank()) {
                return kospi;
            }
            for (IndexType indexType : values()) {
                if(indexType.name().equals(type)) {
                    return indexType;
                }
            }
            return kospi;
        }
    }

    /**
     * 종목 소개 조회
     * @param stockCode
     * @return
     */
    public CustomResponse getCompanyDesc(String stockCode) throws CustomException {
        CompanyDesc companyDesc = companyDescRepository.findByIscd(stockCode)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMON_EMPTY));

        ResponseAiDTO.CompanyDescData response = modelMapper.map(companyDesc, ResponseAiDTO.CompanyDescData.class);

        return CustomResponse.builder()
                .data(response)
                .build();
    }

    /**
     * 종목 주가 그래프 데이터 조회
     * @param stockCode
     * @param calcRange
     * @return
     */
    public CustomResponse getStockGraph(String stockCode, Integer calcRange) {
        // 종목 주가 조회
        List<AiStockValues> StockValuesList = aiStockValuesRepository.findAllByStockCodeAndStockDateGreaterThanEqualOrderByStockDateDesc(stockCode, LocalDate.now().minusMonths(calcRange));

        List<ResponseAiDTO.AiStockPriceChart> response = modelMapper.map(StockValuesList, new TypeToken<List<ResponseAiDTO.AiStockPriceChart>>(){}.getType());

        return CustomResponse.builder()
                .data(response)
                .build();
    }

    /**
     * 안정성 혁신성 분석 조회
     * @param stockCode
     * @param baseYear
     * @param type
     * @return
     * @throws CustomException
     */
    public CustomResponse getInnovation(String stockCode, Integer baseYear, RequestAiDTO.InnovationType type) throws CustomException {
        // 종목 매트릭스 조회
        AiStockInnovationScore score = aiStockInnovationScoreRepository.findByStockCodeAndBaseYear(stockCode, baseYear)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMON_EMPTY));
        // 종목 집계 조회
        ResponseAiMapper.AiInnovationAggregationInterface aggregationInterface;
        // 종목 분포도 조회
        ResponseAiMapper.AiInnovationChartInterface chartInterface;

        switch(type) {
            // 전종목
            case all -> {
                chartInterface = aiStockInnovationScoreRepository.getInnovationAllChart();
                aggregationInterface = aiStockInnovationScoreRepository.getAggregationAll();
            }
            // 코스피
            case kospi -> {
                chartInterface = aiStockInnovationScoreRepository.getInnovationKospiChart();
                aggregationInterface = aiStockInnovationScoreRepository.getAggregationKospi();
            }
            // 코스닥
            case kosdaq -> {
                chartInterface = aiStockInnovationScoreRepository.getInnovationKosdaqChart();
                aggregationInterface = aiStockInnovationScoreRepository.getAggregationKosdaq();
            }
            // 동일업종
            case industry -> {
                JStockJong jStockJong = jstockJongRepository.findTop1ByIscdOrderByCreateDateDesc(stockCode);
                chartInterface = aiStockInnovationScoreRepository.getInnovationIndustryChart(jStockJong.getBstpLargDivCode(), jStockJong.getBstpMedmDivCode(), jStockJong.getBstpSmalDivCode());
                aggregationInterface = aiStockInnovationScoreRepository.getAggregationIndustry(jStockJong.getBstpLargDivCode(), jStockJong.getBstpMedmDivCode(), jStockJong.getBstpSmalDivCode());
            }
            // 동일테마
            case theme -> {
                // 해당 종목의 테마 idx 뽑기
                List<ThemeOriginal> themeAllList = themeOriginalRepository.findAllByIscd(stockCode);
                List<Long> themeIdxList = themeAllList.stream()
                        .map(ThemeOriginal::getThemeIdx)
                        .toList();
                // 테마 idx에 해당하는 stockCode 뽑기
                List<ThemeOriginal> themeList = themeOriginalRepository.findAllByThemeIdxIn(themeIdxList);
                List<String> stockCodeList = themeList.stream()
                        .map(ThemeOriginal::getIscd)
                        .toList();
                chartInterface = aiStockInnovationScoreRepository.getInnovationThemeChart(stockCodeList);
                aggregationInterface = aiStockInnovationScoreRepository.getAggregationTheme(stockCodeList);
            }
            default -> throw new CustomException(ErrorCode.COMMON_INVALID);
        }

        ResponseAiDTO.AiInnovationResultData response = ResponseAiDTO.AiInnovationResultData.builder()
                .matrix(new ResponseAiDTO.AiInnovationMatrix(score))
                .aggregation(new ResponseAiDTO.AiInnovationAggregation(aggregationInterface))
                .chart(new ResponseAiDTO.AiInnovationChart(chartInterface))
                .build();
        return CustomResponse.builder()
                .data(response)
                .build();
    }

    /**
     * 유사패턴 종목 목록 및 그래프 조회
     * @param stockCode
     * @param calcRange
     * @return
     */
    public CustomResponse getCorrelation(String stockCode, Integer calcRange) {
        // 유사패턴 상위 목록 조회
        List<ResponseAiMapper.AiCorrelationInterface> correlationInterface = aiStockCorrelationRepository.getCorrelationLimit(stockCode, calcRange, 10);
        List<ResponseAiDTO.AiCorrelationData> top10List = correlationInterface.stream()
                .map(ResponseAiDTO.AiCorrelationData::new)
                .toList();

        // 유사패턴 상위 차트 조회
        HashMap<String, List<ResponseAiDTO.AiCorrelationChart>> stockValueMap = new HashMap<>();
        correlationInterface.forEach(aiCorrelationInterface -> {
            List<AiStockValues> stockValueList = aiStockValuesRepository.findAllByStockCodeAndStockDateGreaterThanEqualOrderByStockDateDesc(aiCorrelationInterface.getStockCode(), LocalDate.now().minusMonths(calcRange));
            stockValueMap.put(
                    aiCorrelationInterface.getStockCode(),
                    modelMapper.map(stockValueList, new TypeToken<List<ResponseAiDTO.AiCorrelationChart>>(){}.getType())
            );
        });
        ResponseAiDTO.AiCorrelationResultData response = ResponseAiDTO.AiCorrelationResultData.builder()
                .top10List(top10List)
                .chartList(stockValueMap)
                .build();

        return CustomResponse.builder()
                .data(response)
                .build();
    }

    /**
     * 전자공시 조회(당월 포함 1년)
     * @param pageable
     * @param stockCode
     * @return
     */
    public CustomResponse getDisclosureAnalysisList(Pageable pageable, String stockCode) {
        LocalDate now = LocalDate.now();
        LocalDate pre = now.minusMonths(11);
        pre = LocalDate.of(pre.getYear(), pre.getMonth(), 1);
        Page<AiDartAnalysis> dartAnalysisList = aiDartAnalysisRepository.findByStockCodeAndRceptDtGreaterThanEqualAndRceptDtLessThanEqualOrderByRceptDtDesc(stockCode, pre, now, pageable);

        List<ResponseAiDTO.AiDartAnalysis> aiDartAnalysisList = new ArrayList<>();
        for(AiDartAnalysis entity : dartAnalysisList) {
            ResponseAiDTO.AiDartAnalysis aiDartAnalysis = modelMapper.map(entity, ResponseAiDTO.AiDartAnalysis.class);
            JSise jSise = jsiseRepository.findTop1ByIscdAndBsopDate(stockCode, Integer.parseInt(entity.getRceptDt().format(DateTimeFormatter.ofPattern("yyyyMMdd"))));
            if(jSise != null) {
                aiDartAnalysis.setPrpr(jSise.getPrpr());
                aiDartAnalysis.setPrdyCtrt(jSise.getPrdyCtrt());
                aiDartAnalysisList.add(aiDartAnalysis);
            } else {
                aiDartAnalysis.setPrpr(null);
                aiDartAnalysis.setPrdyCtrt(null);
                aiDartAnalysisList.add(aiDartAnalysis);
            }
        }
        return CustomResponse.builder()
                .status(ErrorCode.OK.getStatus())
                .message(ErrorCode.OK.getMessage())
                .data(ResponseAiDTO.AiDartAnalysisResultData.builder()
                        .pageable(pageable)
                        .totalPages(dartAnalysisList.getTotalPages())
                        .totalElements(dartAnalysisList.getTotalElements())
                        .analysisList(aiDartAnalysisList)
                        .analysisCount(ResponseAiDTO.AiDartAnalysisTotalCount.builder()
                                .total(aiDartAnalysisRepository.countByStockCodeAndRceptDtGreaterThanEqualAndRceptDtLessThanEqual(stockCode, pre, now))
                                .positiveTotal(aiDartAnalysisRepository.countByStockCodeAndSentimentAndRceptDtGreaterThanEqualAndRceptDtLessThanEqual(stockCode, EnumCode.Ai.DartAnalysisCode.Positive.getValue(), pre, now))
                                .negativeTotal(aiDartAnalysisRepository.countByStockCodeAndSentimentAndRceptDtGreaterThanEqualAndRceptDtLessThanEqual(stockCode, EnumCode.Ai.DartAnalysisCode.Negative.getValue(), pre, now))
                                .neutralityTotal(aiDartAnalysisRepository.countByStockCodeAndSentimentAndRceptDtGreaterThanEqualAndRceptDtLessThanEqual(stockCode, EnumCode.Ai.DartAnalysisCode.Neutrality.getValue(), pre, now))
                                .build())
                        .build())
                .build();
    }

    /**
     * 전자공시 차트 조회(당월 포함 1년)
     * @param stockCode
     * @return
     */
    public CustomResponse getDisclosureAnalysisGraph(String stockCode) {
        ResponseAiDTO.AiDartAnalysisChartList response = new ResponseAiDTO.AiDartAnalysisChartList();
        response.setStockCode(stockCode);

        LocalDate now = LocalDate.now();
        LocalDate pre = now.minusMonths(11);
        pre = LocalDate.of(pre.getYear(), pre.getMonth(), 1);

        // 긍정
        List<ResponseAiMapper.AiDartAnalysisChartInterface> positiveMapperList = aiDartAnalysisRepository.findTop12ByStockCodeAndSentimentAndRceptDtOrderByRceptDtDesc(stockCode, now, pre, EnumCode.Ai.DartAnalysisCode.Positive.getValue());

        List<ResponseAiDTO.AiDartAnalysisChart> positiveList = setAiDartAnalysisChart(positiveMapperList, EnumCode.Ai.DartAnalysisCode.Positive.getValue());
        response.setPositiveChartList(positiveList);

        // 부정
        List<ResponseAiMapper.AiDartAnalysisChartInterface> negativeMapperList = aiDartAnalysisRepository.findTop12ByStockCodeAndSentimentAndRceptDtOrderByRceptDtDesc(stockCode, now, pre, EnumCode.Ai.DartAnalysisCode.Negative.getValue());

        List<ResponseAiDTO.AiDartAnalysisChart> negativeList = setAiDartAnalysisChart(negativeMapperList, EnumCode.Ai.DartAnalysisCode.Negative.getValue());
        response.setNegativeChartList(negativeList);

        // 중립
        List<ResponseAiMapper.AiDartAnalysisChartInterface> neutralityMapperList = aiDartAnalysisRepository.findTop12ByStockCodeAndSentimentAndRceptDtOrderByRceptDtDesc(stockCode, now, pre, EnumCode.Ai.DartAnalysisCode.Neutrality.getValue());

        List<ResponseAiDTO.AiDartAnalysisChart> neutralityList = setAiDartAnalysisChart(neutralityMapperList, EnumCode.Ai.DartAnalysisCode.Neutrality.getValue());
        response.setNeutralityList(neutralityList);

        return CustomResponse.builder()
                .status(ErrorCode.OK.getStatus())
                .message(ErrorCode.OK.getMessage())
                .data(response)
                .build();
    }

    /**
     * 전자공시 차트 맵핑
     * @param list
     * @param sentiment
     * @return
     */
    public List<ResponseAiDTO.AiDartAnalysisChart> setAiDartAnalysisChart(List<ResponseAiMapper.AiDartAnalysisChartInterface> list, String sentiment) {
        List<ResponseAiDTO.AiDartAnalysisChart> response = new ArrayList<>();
        // 당월부터 1년전 저장된 날짜 세팅
        List<String> dateList = new ArrayList<>();
        for(int i = 0; i < 12; i++) {
            dateList.add(LocalDate.now().minusMonths(i).format(DateTimeFormatter.ofPattern("yyyy-MM")));
            ResponseAiDTO.AiDartAnalysisChart data = new ResponseAiDTO.AiDartAnalysisChart();
            response.add(data);
        }

        for (int i = 0; i < dateList.size(); i++) {
            for(int j = 0; j < list.size(); j++) {
                if(dateList.get(i).equals(list.get(j).getYearMonth())) {
                    response.get(i).setYearMonth(list.get(j).getYearMonth());
                    response.get(i).setCnt(list.get(j).getCnt());
                    response.get(i).setSentiment(list.get(j).getSentiment());
                    i++;
                }
            }
            if(i < dateList.size()) {
                if(response.get(i).getYearMonth() == null) {
                    response.get(i).setSentiment(sentiment);
                    response.get(i).setCnt(0);
                    response.get(i).setYearMonth(dateList.get(i));
                }
            }
        }
        return response;
    }

    /**
     * 거래처 목록 조회
     * @param stockCode
     * @param pageable
     * @return
     */
    public CustomResponse getTradeList(String stockCode, Pageable pageable) {
        List<AiDartOrderRcept> aiDartOrderRceptList = aiDartOrderRceptRepository.findAllByStockCode(stockCode, pageable);
        Long totalCount = aiDartOrderRceptRepository.countAllByStockCode(stockCode);
        List<ResponseAiDTO.AiDartOrderRcept> response = modelMapper.map(aiDartOrderRceptList, new TypeToken<List<ResponseAiDTO.AiDartOrderRcept>>(){}.getType());
        return CustomResponse.builder()
                .data(new PageImpl<>(response, pageable, totalCount))
                .build();
    }

    /**
     * 한국종합주가 지수, 미국종합주가 지수, 유가 지수, 달러환율 지수 그래프 분석 조회
     * @param stockCode
     * @param calcRange
     * @return
     */
    public CustomResponse getIntegrationIndexGraph(String stockCode, Integer calcRange) throws CustomException {
        // 종목과 지수간의 상관관계 데이터 조회
        AiStockIndexCorrelation aiStockIndexCorrelation = aiStockIndexCorrelationRepository.findTop1ByStockCodeAndCalcRangeOrderByStockDateDesc(stockCode, calcRange)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMON_EMPTY));
        // 종목 주가 목록 기간 조회(금융지수의 날짜가 동일하게 있는것만)
        List<AiStockValues> aiStockValuesList = aiStockValuesRepository.getNotEmptyStockDateFinanceIndexes(stockCode, LocalDate.now().minusMonths(calcRange));

        // 종목 주가의 날짜만 추출
        List<LocalDate> stockDateList = aiStockValuesList.stream()
                .map(AiStockValues::getStockDate)
                .toList();
        // 주가가 포함된 날짜의 금융지수 목록 조회
        List<AiFinanceIndexes> financeIndexesList = aiFinanceIndexesRepository.findAllByStockDateInOrderByStockDateDesc(stockDateList);
        // 응답 구조 생성
        ResponseAiDTO.AiIndexCorrelationResultData response = modelMapper.map(aiStockIndexCorrelation, ResponseAiDTO.AiIndexCorrelationResultData.class);
        // 종목 주가 목록 추가
        response.setStockChartList(modelMapper.map(aiStockValuesList, new TypeToken<List<ResponseAiDTO.AiStockPriceChart>>(){}.getType()));
        switch(IndexType.stringToEnum(aiStockIndexCorrelation.getKoIndex())) {
            case kospi -> {
                // indexType이 kospi일 경우 dji로 데이터 추가
                financeIndexesList
                        .forEach(aiFinanceIndexes -> {
                            // 한국 종합주가지수 추가
                            response.getKoChartList().add(
                                    ResponseAiDTO.AiIndexChart.builder()
                                            .stockDate(aiFinanceIndexes.getStockDate())
                                            .close(aiFinanceIndexes.getKospi().doubleValue())
                                            .build()
                            );
                            // 미국 종합주가지수 추가
                            response.getUsChartList().add(
                                    ResponseAiDTO.AiIndexChart.builder()
                                            .stockDate(aiFinanceIndexes.getStockDate())
                                            .close(aiFinanceIndexes.getDji().doubleValue())
                                            .build()
                            );
                            // 원달러 활율 지수 추가
                            response.getUsdKrwChartList().add(
                                    ResponseAiDTO.AiIndexChart.builder()
                                            .stockDate(aiFinanceIndexes.getStockDate())
                                            .close(aiFinanceIndexes.getUsdKrw().doubleValue())
                                            .build()
                            );
                        });
            }
            case kosdaq -> {
                // indexType이 kosdaq일 경우 nasdaq로 조회
                financeIndexesList.forEach(aiFinanceIndexes -> {
                    // 한국 종합주가지수 추가
                    response.getKoChartList().add(
                            ResponseAiDTO.AiIndexChart.builder()
                                    .stockDate(aiFinanceIndexes.getStockDate())
                                    .close(aiFinanceIndexes.getKosdaq().doubleValue())
                                    .build()
                    );
                    // 미국 종합주가지수 추가
                    response.getUsChartList().add(
                            ResponseAiDTO.AiIndexChart.builder()
                                    .stockDate(aiFinanceIndexes.getStockDate())
                                    .close(aiFinanceIndexes.getNasdaq().doubleValue())
                                    .build()
                    );
                    // 원달러 활율 지수 추가
                    response.getUsdKrwChartList().add(
                            ResponseAiDTO.AiIndexChart.builder()
                                    .stockDate(aiFinanceIndexes.getStockDate())
                                    .close(aiFinanceIndexes.getUsdKrw().doubleValue())
                                    .build()
                    );
                });
            }
        }

        List<AiOilIndex> oilIndexList = aiOilIndexRepository.findAllByStockDateInOrderByStockDateDesc(stockDateList);
        oilIndexList.forEach(aiOilIndex -> {
            // 국제유가 지수 추가
            response.getWtiChartList().add(
                    ResponseAiDTO.AiIndexChart.builder()
                            .stockDate(aiOilIndex.getStockDate())
                            .close(aiOilIndex.getWti())
                            .build()
            );
        });

        return CustomResponse.builder()
                .data(response)
                .build();
    }
}
