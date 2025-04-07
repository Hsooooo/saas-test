package com.illunex.emsaasrestapi.ai.dto;

import java.time.LocalDate;

public class ResponseAiMapper {
    /**
     * 안정성 혁신성 분석(종목진단결과 분포도 집계) 맵핑용 인터페이스
     */
    public interface AiInnovationAggregationInterface {
        Double getMin();
        Double getAvg();
        Double getMax();
    }

    /**
     * 안정성 혁신성 분석(종목진단결과 분포도 차트) 맵핑용 데이터
     */
    public interface AiInnovationChartInterface {
        Integer getScore10();
        Integer getScore20();
        Integer getScore30();
        Integer getScore40();
        Integer getScore50();
        Integer getScore60();
        Integer getScore70();
        Integer getScore80();
        Integer getScore90();
        Integer getScore100();
        Integer getTotal();
    }

    /**
     * 유사패턴 종목 Top10 맵핑용 인터페이스
     */
    public interface AiCorrelationInterface {
        String getStockCode();                      // 단축종목크
        String getKorIsnm();                        // 종목명
        String getThemeNames();                     // 테마 여러개
        Integer getStockClose();                    // 대상 종가
        Double getStockIncRate();                   // 대상 상승률
        Double getCorrelation();                    // 상관 계수(유사도)
        Integer getCalcRange();                     // 계산범위(단위 : 월)
        Integer getLag();                           // 선행지수(단위 : 일)
        LocalDate getStockDate();                   // 기준날짜
    }

    /**
     * 전자공시 긍정/부정/중립 추이 차트 맵핑용 데이터
     */
    public interface AiDartAnalysisChartInterface {
        String getYearMonth();
        String getSentiment();
        Integer getCnt();
    }
}
