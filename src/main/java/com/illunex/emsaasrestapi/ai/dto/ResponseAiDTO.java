package com.illunex.emsaasrestapi.ai.dto;

import com.illunex.emsaasrestapi.ai.entity.AiStockInnovationScore;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ResponseAiDTO {

    /**
     * 종목 소개 데이터
     */
    @Getter
    @Setter
    @RequiredArgsConstructor
    public static class CompanyDescData {
        private Integer id;                             // 고유번호
        private String iscd;                            // 단축종목코드
        private String descript;                        // 종목 소개
        private LocalDateTime updateDate;               // 수정일
        private LocalDateTime createDate;               // 생성일
    }

    /**
     * 안정성 혁신성 분석 응답 데이터
     */
    @Getter
    @Builder
    public static class AiInnovationResultData {
        private AiInnovationMatrix matrix;
        private AiInnovationAggregation aggregation;
        private AiInnovationChart chart;
    }

    /**
     * 유사패턴 종목 Top10 응답 데이터
     */
    @Getter
    @Builder
    public static class AiCorrelationResultData {
        List<AiCorrelationData> top10List;
        HashMap<String, List<AiCorrelationChart>> chartList;
    }

    /**
     * 전자공시 응답 데이터
     */
    @Getter
    @Builder
    public static class AiDartAnalysisResultData {
        private Pageable pageable; // 페이지 조회 정보
        private int totalPages; // 총 페이지 수
        private long totalElements; // 총 데이터 수
        private List<AiDartAnalysis> analysisList; // 전자공시 목록
        private AiDartAnalysisTotalCount analysisCount; // 각 전자공시 긍정, 부정, 중립 건수, 총 건수
    }

    /**
     * 종목 지수 차트, 종합 지수 차트
     */
    @Getter
    @Setter
    public static class AiIndexCorrelationResultData {
        private String koIndex;                                     // 한국종합주가지수 구분(kospi, kosdaq)
        private String usIndex;                                     // 미국종합주가지수 구분(djia, nasdaq)
        private Double koIndexCorrelation;                          // 한국종합주가지수 상관 계수
        private Integer koIndexLag;                                 // 한국종합주가지수 lag(단위 : 일)
        private Double usIndexCorrelation;                          // 미국종합주가지수 상관 계수
        private Integer usIndexLag;                                 // 미국종합주가지수 lag(단위 : 일)
        private Double usdKrwCorrelation;                           // 원달러 환율 상관 계수
        private Integer usdKrwLag;                                  // 원달러 환율 lag(단위 : 일)
        private Double wtiCorrelation;                              // WTI 상관계수(단위 : 일)
        private Integer wtiLag;                                     // WTI lag(단위 : 일)

        private List<AiStockPriceChart> stockChartList = new ArrayList<>();          // 종목 주가 차트 목록
        private List<AiIndexChart> koChartList = new ArrayList<>();            // 한국종합주가 지수 차트 목록
        private List<AiIndexChart> usChartList = new ArrayList<>();             // 미국종합주가 지수 차트 목록
        private List<AiIndexChart> usdKrwChartList = new ArrayList<>();         // 원달러환율 지수 차트 목록
        private List<AiIndexChart> wtiChartList = new ArrayList<>();            // 국제유가 지수 차트 목록
    }

    /**
     * 종목 주가 차트 데이터
     */
    @Getter
    @Setter
    public static class AiStockPriceChart {
        private LocalDate stockDate;                    // 기준날짜
        private Integer close;                          // 해당일 종가
    }

    /**
     * 안정성 혁신성 분석(종목진단 메트릭스) 데이터
     */
    @Getter
    public static class AiInnovationMatrix {
        private final Long id;
        private final String stockCode;                 // 단축종목코드
        private final String businessNumber;            // 사업자번호
        private final Integer baseYear;                 // 기준년도
        private final Double rsScore;                   // RS점수(혁신 인프라)
        private final Double acScore;                   // AC점수(혁신 활동)
        private final Double ocScore;                   // OC점수(혁신 산출)
        private final Double opScore;                   // OP점수(혁신 성과)
        private final Double xAxis;                     // x축(혁신성 점수)
        private final Double yAxis;                     // y축(안정성 점수)
        private final Integer rank;                     // 전체 순위
        private final Float totalScore;                 // 총 점수
        private final Integer totalGroup;               // x,y축 기반 점수 그룹(1:혁신도약형, 2:안정추구형, 3:혁신성장형, 4:잠재성장형)
        private final String chartPosition;

        public AiInnovationMatrix(AiStockInnovationScore score) {
            this.id = score.getId();
            this.stockCode = score.getStockCode();
            this.businessNumber = score.getBusinessNumber();
            this.baseYear = score.getBaseYear();
            this.rsScore = score.getRsScore();
            this.acScore = score.getAcScore();
            this.ocScore = score.getOcScore();
            this.opScore = score.getOpScore();
            this.xAxis = score.getXAxis();
            this.yAxis = score.getYAxis();
            this.rank = score.getRank();
            this.totalScore = score.getTotalScore();
            this.totalGroup = score.getTotalGroup();
            // 분포도 위치 키값 설정
            Integer position = (int)(Math.floor(score.getTotalScore() / 10) + 1) * 10;
            this.chartPosition = "score" + position;
        }
    }

    /**
     * 안정성 혁신성 분석(종목진단분포도 집계) 데이터
     */
    @Getter
    public static class AiInnovationAggregation {
        private final Double min;
        private final Double avg;
        private final Double max;
        public AiInnovationAggregation(ResponseAiMapper.AiInnovationAggregationInterface entity) {
            this.min = entity.getMin();
            this.avg = entity.getAvg();
            this.max = entity.getMax();
        }
    }

    /**
     * 안정성 혁신성 분석(종목진단분포도 차트) 데이터
     */
    @Getter
    public static class AiInnovationChart {
        private final Integer score10;                  // 0~10점 분포 개수
        private final Integer score20;                  // 10~20점 분포 개수
        private final Integer score30;                  // 20~30점 분포 개수
        private final Integer score40;                  // 30~40점 분포 개수
        private final Integer score50;                  // 40~50점 분포 개수
        private final Integer score60;                  // 50~60점 분포 개수
        private final Integer score70;                  // 60~70점 분포 개수
        private final Integer score80;                  // 70~80점 분포 개수
        private final Integer score90;                  // 80~90점 분포 개수
        private final Integer score100;                 // 90~100점 분포 개수
        private final Integer total;                    // 총 개수

        public AiInnovationChart(ResponseAiMapper.AiInnovationChartInterface entity) {
            this.score10 = entity.getScore10();
            this.score20 = entity.getScore20();
            this.score30 = entity.getScore30();
            this.score40 = entity.getScore40();
            this.score50 = entity.getScore50();
            this.score60 = entity.getScore60();
            this.score70 = entity.getScore70();
            this.score80 = entity.getScore80();
            this.score90 = entity.getScore90();
            this.score100 = entity.getScore100();
            this.total = entity.getTotal();
        }
    }

    /**
     * 유사패턴 종목 Top10 단일 데이터
     */
    @Getter
    public static class AiCorrelationData {
        private final String stockCode;                      // 단축종목코드
        private final String korIsnm;                        // 종목명
        private final String themeNames;                     // 테마 여러개
        private final Integer stockClose;                    // 대상 종가
        private final Double stockIncRate;                   // 대상 상승률
        private final Double correlation;                    // 상관 계수(유사도)
        private final Integer calcRange;                     // 계산범위(단위 : 월)
        private final Integer lag;                           // 선행지수(단위 : 일)
        private final LocalDate stockDate;                   // 기준날짜

        public AiCorrelationData(ResponseAiMapper.AiCorrelationInterface entity) {
            this.stockCode = entity.getStockCode();
            this.korIsnm = entity.getKorIsnm();
            this.themeNames = entity.getThemeNames();
            this.stockClose = entity.getStockClose();
            this.stockIncRate = entity.getStockIncRate();
            this.correlation = entity.getCorrelation();
            this.calcRange = entity.getCalcRange();
            this.lag = entity.getLag();
            this.stockDate = entity.getStockDate();
        }
    }

    /**
     * 유사패턴 종목 Top10 차트 데이터
     */
    @Getter
    @Setter
    public static class AiCorrelationChart {
        private LocalDate stockDate;
        private Double stdClose;
    }

    /**
     * 각 전자공시 긍정, 부정, 중립 건수, 총 건수
     */
    @Getter
    @Builder
    public static class AiDartAnalysisTotalCount {
        private int total; // 긍정/부정/중립 총 건수
        private int positiveTotal; // 총 긍정 건수
        private int negativeTotal; // 총 부정 건수
        private int neutralityTotal; // 총 중립 건수
    }
    /**
     * 전자공시
     */
    @Getter
    @Setter
    public static class AiDartAnalysis {
        private Long id; // 공시분석 번호
        private String corpName; // 법인 이름
        private String corpCode; // dart 고유번호
        private String stockCode; // 단축종목코드
        private String reportNm; // 보고서 이름
        private String rceptNo; // 공시번호
        private LocalDate rceptDt; // 공시날짜
        private String category; // 카테고리 분류
        private String sentiment; // 감성분석
        private String summary; // 내용 요약
        private Integer prpr; // 전자공시 날짜에 대한 주가
        private Double prdyCtrt; // 전자공시 날짜에 대한 전일 대비율(단위 : %)
        private String siteUrl; // 사이트 링크
        public void setRceptNo(String rceptNo) {
            this.rceptNo = rceptNo;
            this.siteUrl = "https://dart.fss.or.kr/dsaf001/main.do?rcpNo=" + rceptNo;
        }
    }

    /**
     * 긍정, 중립, 부정 지수 추이
     */
    @Getter
    @Setter
    public static class AiDartAnalysisChartList {
        private String stockCode; // 단축종목코드
        private List<AiDartAnalysisChart> positiveChartList; // 긍정 차트 목록
        private List<AiDartAnalysisChart> negativeChartList; // 부정 차트 목록
        private List<AiDartAnalysisChart> neutralityList; // 중립 차트 목록
    }

    /**
     * 긍정, 중립, 부정 차트
     */
    @Getter
    @Setter
    public static class AiDartAnalysisChart {
        private String yearMonth;
        private String sentiment;
        private int cnt;
    }

    /**
     * 수주공시(거래처)
     */
    @Getter
    @Setter
    @RequiredArgsConstructor
    public static class AiDartOrderRcept {
        private String corpName;                    // 기업명
        private String corpCode;                    // dart고유번호
        private String stockCode;                   // 단축종목코드
        private String reportNm;                    // 보고서 이름
        private String rceptNo;                     // 공시번호
        private LocalDate rceptDt;                  // 공시날짜
        private String targetCompany;               // 계약 대상
        private String contractAmount;              // 계약 금액
        private Double contractAmountRatio;         // 매출 대비 계약 금액 비율
        private String contractStartDate;           // 계약 시작일
        private String contractEndDate;             // 계약 종료일
        private Float contractRange;                // 계약기간(단위 : 월)
        private LocalDate created;                  // 생성일
        private String siteUrl;                     // dart 상세 페이지 URL

        public void setRceptNo(String rceptNo) {
            this.rceptNo = rceptNo;
            this.siteUrl = "https://dart.fss.or.kr/dsaf001/main.do?rcpNo=" + rceptNo;
        }
    }

    /**
     * 한국종합주가 지수, 미국종합주가 지수, 국제유가 지수, 달러환율 지수
     */
    @Getter
    @Builder
    public static class AiIndexChart {
        private LocalDate stockDate;            // 거래날짜
        private Double close;               // 금액(종목 종가, kospi, kosdaq, dji, nasdaq, wti)
    }
}
