package com.illunex.emsaasrestapi.hts;

import com.illunex.emsaasrestapi.common.CustomException;
import com.illunex.emsaasrestapi.common.CustomResponse;
import com.illunex.emsaasrestapi.common.ErrorCode;
import com.illunex.emsaasrestapi.company.entity.CompanyInfo;
import com.illunex.emsaasrestapi.company.entity.CompanyTheme;
import com.illunex.emsaasrestapi.company.repository.CompanyInfoRepository;
import com.illunex.emsaasrestapi.company.repository.CompanyThemeRepository;
import com.illunex.emsaasrestapi.finance.entity.FinanceOtherAnalysis;
import com.illunex.emsaasrestapi.finance.entity.FinanceProfitabilityAnalysis;
import com.illunex.emsaasrestapi.finance.entity.FinanceStockAnalysis;
import com.illunex.emsaasrestapi.finance.repository.FinanceOtherAnalysisRepository;
import com.illunex.emsaasrestapi.finance.repository.FinanceProfitabilityAnalysisRepository;
import com.illunex.emsaasrestapi.finance.repository.FinanceStockAnalysisRepository;
import com.illunex.emsaasrestapi.hts.dto.RequestHtsDTO;
import com.illunex.emsaasrestapi.hts.dto.ResponseHtsDTO;
import com.illunex.emsaasrestapi.hts.dto.ResponseHtsMapper;
import com.illunex.emsaasrestapi.hts.entity.*;
import com.illunex.emsaasrestapi.hts.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.illunex.emsaasrestapi.common.ErrorCode.COMMON_INVALID;

@Slf4j
@RequiredArgsConstructor
@Service
public class SiseService {
    private final JsiseRepository jsiseRepository;
    private final CompanyInfoRepository companyInfoRepository;
    private final InvestorTrendsRepository investorTrendsRepository;
    private final CompanyThemeRepository companyThemeRepository;
    private final ThemeCodeRepository themeCodeRepository;
    private final ThemeOriginalRepository themeOriginalRepository;
    private final JsiseCustomRepository jsiseCustomRepository;
    private final JstockJongRepository jstockJongRepository;
    private final KsicCategoryRepository ksicCategoryRepository;
    private final KsicCompanyRepository ksicCompanyRepository;
    private final JsiseRealTimeRepository jsiseRealTimeRepository;
    private final FinanceStockAnalysisRepository financeStockAnalysisRepository;
    private final FinanceOtherAnalysisRepository financeOtherAnalysisRepository;
    private final FinanceProfitabilityAnalysisRepository financeProfitabilityAnalysisRepository;
    private final ModelMapper modelMapper;

    /**
     * 종목별 상세정보 - 종목정보 탭
     * @param iscd
     * @return
     */
    public CustomResponse getJSiseDetail(String iscd) throws CustomException {
        JSise jsise = jsiseRepository.findTop1ByIscdOrderByBsopDateDescBsopHourDesc(iscd)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMON_EMPTY, ErrorCode.COMMON_EMPTY.getMessage()));
        ResponseHtsDTO.JstockJongDetail response = new ResponseHtsDTO.JstockJongDetail();
        CompanyInfo companyInfo = companyInfoRepository.findByIscd(iscd);
        InvestorTrends investorTrends = investorTrendsRepository.findTop1ByIscdOrderByInvestorDateDesc(iscd);
        JStockJong jStockJong = jstockJongRepository.findTop1ByIscdOrderByCreateDateDesc(iscd);
        List<CompanyTheme> companyThemeList = companyThemeRepository.findByIscdOrderByThemeIdx(iscd);
        List<Long> themeCodeIdxList = new ArrayList<>();
        List<ThemeCode> themeCodeList = new ArrayList<>();

        for(CompanyTheme companyTheme : companyThemeList) {
            themeCodeIdxList.add(companyTheme.getThemeIdx());
        }
        if(themeCodeIdxList.size() > 0) {
            themeCodeList = themeCodeRepository.findByIdxIn(themeCodeIdxList);
        }

        if(jsise != null) {
            response.setJSise(modelMapper.map(jsise, ResponseHtsDTO.JSise.class));
        }

        if(companyInfo != null) {
            response.setCompanyInfo(modelMapper.map(companyInfo, ResponseHtsDTO.CompanyInfo.class));
        }

        if(investorTrends != null) {
            response.setInvestorTrend(modelMapper.map(investorTrends, ResponseHtsDTO.JjongInvestor.class));
        }

        if(themeCodeList.size() > 0) {
            response.setThemeCodeList(modelMapper.map(themeCodeList, new TypeToken<List<ResponseHtsDTO.ThemeCode>>(){}.getType()));
        }

        if(jStockJong != null) {
            response.setJStockJong(modelMapper.map(jStockJong, ResponseHtsDTO.JStockJong.class));
        }
        return CustomResponse.builder()
                .data(response)
                .status(ErrorCode.OK.getStatus())
                .message(ErrorCode.OK.getMessage())
                .build();
    }

    /**
     * 산업분류번호에 해당하는 종목&시세 목록 조회
     * @param ksicCategoryIdx
     * @param pageable
     * @return
     */
    public CustomResponse getKsicJongSiseList(Long ksicCategoryIdx, Pageable pageable) {
        List<String> iscdList = ksicCompanyRepository.findAllByKsicIdx(ksicCategoryIdx, pageable).stream()
                .map(KsicCompany::getIscd).toList();
        Page<ResponseHtsMapper.JsiseRealTimeInerface> response = jsiseRealTimeRepository.findByJsiseRealTimeIscdInAndfinanceStockAnalysis(iscdList, "1", pageable);
        return CustomResponse.builder()
                .message(ErrorCode.OK.getMessage())
                .status(ErrorCode.OK.getStatus())
                .data(response)
                .build();
    }

    /**
     * 테마번호에 해당하는 종목&시세 목록 조회
     * @param themeIdx
     * @param pageable
     * @return
     */
    public CustomResponse getThemeJongSiseList(Long themeIdx, Pageable pageable) {
        List<String> iscdList = themeOriginalRepository.findAllByThemeIdx(themeIdx, pageable).stream()
                .map(ThemeOriginal::getIscd).toList();
        Page<ResponseHtsMapper.JsiseRealTimeInerface> response = jsiseRealTimeRepository.findByJsiseRealTimeIscdInAndfinanceStockAnalysis(iscdList, "1", pageable);
        return CustomResponse.builder()
                .message(ErrorCode.OK.getMessage())
                .status(ErrorCode.OK.getStatus())
                .data(response)
                .build();
    }

    /**
     * 종목별 일별 시세 조회
     * @param iscd
     * @param pageable
     * @return
     */
    public CustomResponse getDailySise(String iscd, Pageable pageable) throws CustomException {
        if(iscd.isEmpty()) {
            throw new CustomException(COMMON_INVALID, COMMON_INVALID.getMessage());
        }
        Page<JSise> jSiseList = jsiseCustomRepository.findAllByIscd(iscd, pageable);
        return CustomResponse.builder()
                .data(modelMapper.map(jSiseList, new TypeToken<Page<ResponseHtsDTO.JSise>>(){}.getType()))
                .message(ErrorCode.OK.getMessage())
                .status(ErrorCode.OK.getStatus())
                .build();
    }

    /**
     * 종목별 상세정보 - 차트 탭(일별 차트)
     * @param iscd
     * @param pageable
     * @return
     */
    public CustomResponse getJSiseDetailChart(String iscd, Pageable pageable) throws CustomException {
        if(iscd.isEmpty()) {
            throw new CustomException(COMMON_INVALID, COMMON_INVALID.getMessage());
        }
        Page<ResponseHtsDTO.JsiseChart> jSiseChartList = jsiseCustomRepository.findAllJsiseChartByIscd(iscd, pageable);
        return CustomResponse.builder()
                .data(jSiseChartList)
                .message(ErrorCode.OK.getMessage())
                .status(ErrorCode.OK.getStatus())
                .build();
    }

    /**
     * 산업분류별 등락률 조회
     * @return
     */
    public CustomResponse getPrdyCtrtKsic() {
        List<KsicCategory> ksicCategoryList = ksicCategoryRepository.findAll();
        List<ResponseHtsDTO.KsicCategory> response = new ArrayList<>();
        for(int i = 0; i < ksicCategoryList.size(); i++) {
            KsicCompany ksicCompany = ksicCompanyRepository.findTop1ByKsicIdx(ksicCategoryList.get(i).getIdx());
            if(ksicCompany != null) {
                ResponseHtsDTO.KsicCategory ksicCategory = modelMapper.map(ksicCompany, ResponseHtsDTO.KsicCategory.class);
                ksicCategory.setKsicCategoryIdx(ksicCompany.getKsicIdx());
                ksicCategory.setPrdyCtrt(ksicCategoryList.get(i).getPrdyCtrt());
                ksicCategory.setScrapDate(ksicCategoryList.get(i).getScrapDate());
                ksicCategory.setCreateDate(ksicCategoryList.get(i).getCreateDate());
                ksicCategory.setUpdateDate(ksicCategoryList.get(i).getUpdateDate());
                response.add(ksicCategory);
            }
        }
        return CustomResponse.builder()
                .data(response)
                .message(ErrorCode.OK.getMessage())
                .status(ErrorCode.OK.getStatus())
                .build();
    }

    /**
     * 테마별 등락률 조회
     * @return
     */
    public CustomResponse getPrdyCtrtTheme() {
        List<ThemeCode> themeCodeList = themeCodeRepository.findAllByScrapDateIsNotNull();
        return CustomResponse.builder()
                .data(modelMapper.map(themeCodeList, new TypeToken<List<ResponseHtsDTO.ThemeCode>>(){}.getType()))
                .message(ErrorCode.OK.getMessage())
                .status(ErrorCode.OK.getStatus())
                .build();
    }

    /**
     * 10분 지연 시세 조회
     * @return
     */
    public CustomResponse getPrdyCtrtRealTime(RequestHtsDTO.SearchIscds searchIscds) {
        List<ResponseHtsMapper.JsiseRealTimeInerface> response = new ArrayList<>();
        if(searchIscds.getIscds().size() == 0) {
            response = jsiseRealTimeRepository.findAllJsiseRealTimeAndFinanceStockAnalysis("1");
        } else {
            response = jsiseRealTimeRepository.findAllJsiseRealTimeAndFinanceStockAnalysisByIscdIn(searchIscds.getIscds(), "1");
        }
        return CustomResponse.builder()
                .data(response)
                .status(ErrorCode.OK.getStatus())
                .message(ErrorCode.OK.getMessage())
                .build();
    }

    /**
     * 10분 지연 TOP 100 시세 조회
     */
    public CustomResponse getTopRealTime(PageRequest pageable) {
        Page<ResponseHtsMapper.JsiseRealTimeInerface> response = jsiseRealTimeRepository.findJsiseRealTImeJoinFinanceStockAnalysis(pageable, "1");
        return CustomResponse.builder()
                .data(response)
                .message(ErrorCode.OK.getMessage())
                .status(ErrorCode.OK.getStatus())
                .build();
    }

    /**
     * 스톡 페이지 사이드 패널(시세 정보)
     * @param iscd
     * @return
     */
    public CustomResponse getStockSiseInfo(String iscd) {
        JStockJong jStockJong = jstockJongRepository.findTop1ByIscdOrderByCreateDateDesc(iscd);
        ResponseHtsDTO.StockSiseInfo response = new ResponseHtsDTO.StockSiseInfo();
        if(jStockJong != null) {
            JsiseRealTime jsiseRealTime = jsiseRealTimeRepository.findByIscd(iscd);
            if(jsiseRealTime != null) {
                response = modelMapper.map(jsiseRealTime, ResponseHtsDTO.StockSiseInfo.class);
                response.setStckFcam(jStockJong.getStckFcam());
                response.setMrktDivClsCode(jStockJong.getMrktDivClsCode());
                response.setLstnStcn(jStockJong.getLstnStcn());

                // 시가총액 순위
                Long rank = jstockJongRepository.findByIscdOrderByRankDesc(iscd, jStockJong.getMrktDivClsCode());
                response.setAvlsRanking(rank);

                // 재무정보 가져오기
                Optional<FinanceStockAnalysis> financeStockAnalysis = financeStockAnalysisRepository.findTop1ByIscdAndConsolidatedOrderByFiscalYearEndDesc(iscd, "1");
                if(financeStockAnalysis.isPresent()) {
                    response.setConsolidated(financeStockAnalysis.get().getConsolidated());
                    response.setFiscalYearEnd(financeStockAnalysis.get().getFiscalYearEnd());
                    response.setEps(financeStockAnalysis.get().getEps());
                    response.setPbr(financeStockAnalysis.get().getPbr());
                    response.setBps(financeStockAnalysis.get().getBps());
                    response.setPer(financeStockAnalysis.get().getPer());
                    response.setDividendYield(financeStockAnalysis.get().getDividendYield());
                    if(financeStockAnalysis.get().getEps() > 0 && jsiseRealTime.getPrpr() > 0) {
                        response.setPes(financeStockAnalysis.get().getEps()/jsiseRealTime.getPrpr());
                    }
                    Optional<FinanceOtherAnalysis> financeOtherAnalysis = financeOtherAnalysisRepository.findByIscdAndConsolidatedAndFiscalYearEnd(iscd, "1", financeStockAnalysis.get().getFiscalYearEnd());
                    if(financeOtherAnalysis.isPresent()) {
                        if(financeOtherAnalysis.get().getRevenue() > 0 && financeStockAnalysis.get().getMarketCap() > 0) {
                            // 주가매출비율 = 시가총액 / 매출액
                            response.setPsr(financeStockAnalysis.get().getMarketCap().doubleValue() / (double)financeOtherAnalysis.get().getRevenue());
                        }
                    }

                    Optional<FinanceProfitabilityAnalysis> financeProfitabilityAnalysis = financeProfitabilityAnalysisRepository.findByIscdAndConsolidatedAndFiscalYearEnd(iscd, "1", financeStockAnalysis.get().getFiscalYearEnd());
                    if(financeProfitabilityAnalysis.isPresent()) {
                        // 자기자본 순이익률 (ROE)
                        response.setRoe(financeProfitabilityAnalysis.get().getReturnOnEquity());
                    }
                }

                // 투자자정보 가져오기
                InvestorTrends investorTrends = investorTrendsRepository.findTop1ByIscdOrderByInvestorDateDesc(iscd);
                if(investorTrends != null) {
                    Long buyVolume = 0L; // 매수 수량
                    Long selVolume = 0L; // 매도 수량
                    // 매수 수량
                    buyVolume += investorTrends.getForeignBuyVolume();
                    buyVolume += investorTrends.getIndividualBuyVolume();
                    buyVolume += investorTrends.getInstitutionalBuyVolume();
                    buyVolume += investorTrends.getInvestmentTrustBuyVolume();
                    buyVolume += investorTrends.getPrivateEquityBuyVolume();
                    buyVolume += investorTrends.getFinancialInvestmentBuyVolume();
                    buyVolume += investorTrends.getInsuranceBuyVolume();
                    buyVolume += investorTrends.getBankBuyVolume();
                    buyVolume += investorTrends.getOtherFinancialBuyVolume();
                    buyVolume += investorTrends.getPensionFundsBuyVolume();
                    buyVolume += investorTrends.getOtherCorporationsBuyVolume();
                    buyVolume += investorTrends.getDomesticAndForeignBuyVolume();
                    response.setBuyVolume(buyVolume);

                    // 매도 수량
                    selVolume += investorTrends.getForeignSellVolume();
                    selVolume += investorTrends.getIndividualSellVolume();
                    selVolume += investorTrends.getInstitutionalSellVolume();
                    selVolume += investorTrends.getInvestmentTrustSellVolume();
                    selVolume += investorTrends.getPrivateEquitySellVolume();
                    selVolume += investorTrends.getFinancialInvestmentSellVolume();
                    selVolume += investorTrends.getInsuranceSellVolume();
                    selVolume += investorTrends.getBankSellVolume();
                    selVolume += investorTrends.getOtherFinancialSellVolume();
                    selVolume += investorTrends.getPensionFundsSellVolume();
                    selVolume += investorTrends.getOtherCorporationsSellVolume();
                    selVolume += investorTrends.getDomesticAndForeignSellVolume();
                    response.setSellVolume(selVolume);
                }
            }

            // 전일 등락률
            Optional<JSise> dayPrdyCtrt = jsiseRepository.findTop1ByIscdOrderByBsopDateDescBsopHourDesc(iscd);
            if(dayPrdyCtrt != null) {
                response.setDayCtrt(dayPrdyCtrt.get().getPrdyCtrt());
                response.setDayBsopDate(dayPrdyCtrt.get().getBsopDate());
                response.setDayBsopHour(dayPrdyCtrt.get().getBsopHour());
            }

            // 주 등락률
            JSise weekPrdyCtrt = jsiseRepository.findTop1ByWeekPrdyCtrt(iscd, jsiseRealTime.getBsopDate());
            if(weekPrdyCtrt != null) {
                response.setWeekCtrt(weekPrdyCtrt.getPrdyCtrt());
                response.setWeekBsopDate(weekPrdyCtrt.getBsopDate());
                response.setWeekBsopHour(weekPrdyCtrt.getBsopHour());
            }

            // 월 등락률
            JSise monthPrdyCtrt = jsiseRepository.findTop1ByMonthPrdyCtrt(iscd, jsiseRealTime.getBsopDate());
            if(monthPrdyCtrt != null) {
                response.setMonthCtrt(monthPrdyCtrt.getPrdyCtrt());
                response.setMonthBsopDate(monthPrdyCtrt.getBsopDate());
                response.setMonthBsopHour(monthPrdyCtrt.getBsopHour());

            }
            // 년 등락률
            JSise yearPrdyCtrt = jsiseRepository.findTop1ByYearPrdyCtrt(iscd, jsiseRealTime.getBsopDate());
            if(yearPrdyCtrt != null) {
                response.setYearCtrt(yearPrdyCtrt.getPrdyCtrt());
                response.setYearBsopDate(yearPrdyCtrt.getBsopDate());
                response.setYearBsopHour(yearPrdyCtrt.getBsopHour());
            }
        }
        return CustomResponse.builder()
                .data(response)
                .message(ErrorCode.OK.getMessage())
                .status(ErrorCode.OK.getStatus())
                .build();
    }

    /**
     * 스톡 페이지 종목별 차트
     */
    public CustomResponse getStockChart(RequestHtsDTO.SearchStockChart search) throws CustomException {
        if(search.getIscd().isEmpty()) {
            throw new CustomException(COMMON_INVALID, COMMON_INVALID.getMessage());
        }
        List<ResponseHtsDTO.StockChartGroup> result = jsiseCustomRepository.findStockChart(search.getIscd(), search.getFrom(), search.getCnt());
        return CustomResponse.builder()
                .data(result)
                .message(ErrorCode.OK.getMessage())
                .status(ErrorCode.OK.getStatus())
                .build();
    }
}