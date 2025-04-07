package com.illunex.emsaasrestapi.scrap;

import com.illunex.emsaasrestapi.common.CustomResponse;
import com.illunex.emsaasrestapi.common.ErrorCode;
import com.illunex.emsaasrestapi.common.Utils;
import com.illunex.emsaasrestapi.common.webhook.SynologyRequestDto;
import com.illunex.emsaasrestapi.common.webhook.SynologyWebhook;
import com.illunex.emsaasrestapi.company.repository.CompanyInfoRepository;
import com.illunex.emsaasrestapi.hts.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Slf4j
@Component
public class ScrapAPI {

    @Value("${hts.scrap-host}")
    private String htsScrapDomain;
    @Value("${hts.search-host}")
    private String htsSearchDomain;
    @Value("${debug.mode}")
    private Boolean debugMode;
    private final SynologyWebhook synologyWebhook;
    private final ThemeOriginalRepository themeOriginalRepository;
    private final ThemeCodeRepository themeCodeRepository;
    private final KsicCompanyRepository ksicCompanyRepository;
    private final KsicCategoryRepository ksicCategoryRepository;
    private final JstockJongRepository jstockJongRepository;
    private final CompanyInfoRepository companyInfoRepository;
    private final ModelMapper modelMapper;

    public ScrapAPI(SynologyWebhook synologyWebhook, ThemeOriginalRepository themeOriginalRepository, ThemeCodeRepository themeCodeRepository, KsicCompanyRepository ksicCompanyRepository, KsicCategoryRepository ksicCategoryRepository, JstockJongRepository jstockJongRepository, CompanyInfoRepository companyInfoRepository, ModelMapper modelMapper) {
        this.synologyWebhook = synologyWebhook;
        this.themeOriginalRepository = themeOriginalRepository;
        this.themeCodeRepository = themeCodeRepository;
        this.ksicCompanyRepository = ksicCompanyRepository;
        this.ksicCategoryRepository = ksicCategoryRepository;
        this.jstockJongRepository = jstockJongRepository;
        this.companyInfoRepository = companyInfoRepository;
        this.modelMapper = modelMapper;
    }


    /**
     * 월-금 18시마다 EM STOCK 주식 종목 리스트 및 시세 정보 수집 API 호출
     */
    @Scheduled(cron = "0 30 18 * * MON-FRI")
    public CustomResponse<Object> getScrapJjongJsise() {
        if(!debugMode) {
            try {
                synologyWebhook.sendMessage(
                        "[주식 종목 리스트 & 시세정보] 수집시작",
                        SynologyRequestDto.builder()
                                .text("")
                                .build()
                );
                log.debug(Utils.getLogMaker(Utils.eLogType.SYSTEM), "Scheduled jjong_jsise Start----------------");
                // HttpClient 객체 생성
                HttpClient client = HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(300))  // 연결 타임아웃 설정 (5분)
                        .build();

                // HttpRequest 객체 생성
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(htsScrapDomain + "/scrap/jjong_jsise"))
                        .header("Content-Type", "application/json")
                        .timeout(Duration.ofSeconds(300))  // 요청 타임아웃 설정(5분)
                        .POST(HttpRequest.BodyPublishers.noBody())
                        .build();

                log.debug(Utils.getLogMaker(Utils.eLogType.SYSTEM), "response jjong_jsise Before----------------");

                // 요청 보내고 응답 받기
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                // 응답 코드 확인
                log.debug(Utils.getLogMaker(Utils.eLogType.SCRAP), Integer.toString(response.statusCode()));

                // 응답 내용 출력
                log.debug(Utils.getLogMaker(Utils.eLogType.SCRAP), response.body().replaceAll(",", ",\n"));

                synologyWebhook.sendMessage(
                        "[주식 종목 리스트 & 시세정보] 수집종료",
                        SynologyRequestDto.builder()
                                .text(response.body())
                                .build()
                );

                return CustomResponse.builder()
                        .message(response.body())
                        .status(response.statusCode())
                        .build();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 월-금 18시마다 전체 투자자 동향 API 호출
     * @return
     */
    @Scheduled(cron = "0 0 18 * * MON-FRI")
    public CustomResponse getScrapInvest() {
        if(!debugMode) {
            try {
                log.debug(Utils.getLogMaker(Utils.eLogType.SYSTEM), "Scheduled invest Start----------------");
                // HttpClient 객체 생성
                HttpClient client = HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(300))  // 연결 타임아웃 설정(5분)
                        .build();

                // HttpRequest 객체 생성
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(htsScrapDomain + "/scrap/invest"))
                        .header("Content-Type", "application/json")
                        .timeout(Duration.ofSeconds(300))  // 요청 타임아웃 설정(5분)
                        .POST(HttpRequest.BodyPublishers.noBody())
                        .build();

                log.debug(Utils.getLogMaker(Utils.eLogType.SYSTEM), "response invest Before----------------");

                // 요청 보내고 응답 받기
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                // 응답 코드 확인
                log.debug(Utils.getLogMaker(Utils.eLogType.SCRAP), Integer.toString(response.statusCode()));

                // 응답 내용 출력
                log.debug(Utils.getLogMaker(Utils.eLogType.SCRAP), response.body());

                synologyWebhook.sendMessage(
                        "[전체 투자자 동향] 수집종료",
                        SynologyRequestDto.builder()
                                .text("")
                                .build()
                );

                return CustomResponse.builder()
                        .data(response.body())
                        .status(response.statusCode())
                        .build();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 3,6,9,12월의 첫번째 토요일 새벽 2시에 재무 정보 수집 API 호출
     * @return
     */
    @Scheduled(cron = "0 0 2 ? 3,6,9,12 6#1")
    public CustomResponse getScrapFinanceList() {
        if(!debugMode) {
            try {
                synologyWebhook.sendMessage(
                        "[전체 재무 정보] 수집 시작",
                        SynologyRequestDto.builder()
                                .text("")
                                .build()
                );
                log.debug(Utils.getLogMaker(Utils.eLogType.SYSTEM), "Scheduled financeList Start----------------");
                // HttpClient 객체 생성
                HttpClient client = HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(300))  // 연결 타임아웃 설정(5분)
                        .build();

                // HttpRequest 객체 생성
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(htsScrapDomain + "/scrap/financeList"))
                        .header("Content-Type", "application/json")
                        .timeout(Duration.ofSeconds(7200))  // 요청 타임아웃 설정(2시간)
                        .POST(HttpRequest.BodyPublishers.noBody())
                        .build();

                log.debug(Utils.getLogMaker(Utils.eLogType.SCRAP), htsScrapDomain);

                // 요청 보내고 응답 받기
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                log.debug(Utils.getLogMaker(Utils.eLogType.SYSTEM), "response financeList End----------------");

                // 응답 코드 확인
                log.debug(Utils.getLogMaker(Utils.eLogType.SCRAP), Integer.toString(response.statusCode()));

                // 응답 내용 출력
                log.debug(Utils.getLogMaker(Utils.eLogType.SCRAP), response.body());

                synologyWebhook.sendMessage(
                        "[전체 재무 정보] 수집종료",
                        SynologyRequestDto.builder()
                                .text(response.body())
                                .build()
                );
                return CustomResponse.builder()
                        .data(response.body())
                        .status(response.statusCode())
                        .build();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * 재무정보 수동 수집
     * @param iscd
     * @return
     */
    public CustomResponse getScrapFinance(String iscd) {
        if(!debugMode) {
            try {
                synologyWebhook.sendMessage(
                        "[개별 재무 정보] 수집 시작",
                        SynologyRequestDto.builder()
                                .text("")
                                .build()
                );
                log.debug(Utils.getLogMaker(Utils.eLogType.SYSTEM), "Scheduled finance Start----------------");
                // HttpClient 객체 생성
                HttpClient client = HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(300))  // 연결 타임아웃 설정5(분)
                        .build();

                // JSON 요청 바디 작성
                String jsonInputString = "{\"iscd\": \"" + iscd + "\"}";

                // HttpRequest 객체 생성
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(htsScrapDomain + "/scrap/finance"))
                        .header("Content-Type", "application/json")
                        .timeout(Duration.ofSeconds(300))  // 요청 타임아웃 설정(5분)
                        .POST(HttpRequest.BodyPublishers.ofString(jsonInputString))
                        .build();

                // create post too fast 에러로 인해 1초 대기
                Thread.sleep(1000);

                log.debug(Utils.getLogMaker(Utils.eLogType.SCRAP), htsScrapDomain);

                // 요청 보내고 응답 받기
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                log.debug(Utils.getLogMaker(Utils.eLogType.SYSTEM), "Scheduled finance End----------------");

                // 응답 코드 확인
                log.debug(Utils.getLogMaker(Utils.eLogType.SCRAP), Integer.toString(response.statusCode()));

                // 응답 내용 출력
                log.debug(Utils.getLogMaker(Utils.eLogType.SCRAP), response.body());

                synologyWebhook.sendMessage(
                        "[개별 재무 정보] 수집종료",
                        SynologyRequestDto.builder()
                                .text(response.body())
                                .build()
                );
                return CustomResponse.builder()
                        .data(response.body())
                        .status(response.statusCode())
                        .build();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    /**
     * 실시간 시세 정보 (평일 9시부터 16시까지 10분 주기로 수집)
     * 수집된 시간으로부터 10분이 지났을 경우, DB에 저장된 데이터를 업데이트시킨다.
     */
    @Scheduled(cron = "0 */10 9-18 * * MON-FRI")
    public CustomResponse getScrapLatestJsiseRealTime() {
        if(!debugMode) {
            try {
                log.debug(Utils.getLogMaker(Utils.eLogType.SYSTEM), "Scheduled Jsise Real Time Start----------------");
                // HttpClient 객체 생성
                HttpClient client = HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(300))  // 연결 타임아웃 설정(5분)
                        .build();

                // HttpRequest 객체 생성
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(htsScrapDomain + "/scrap/jsise_realtime"))
                        .header("Content-Type", "application/json")
                        .timeout(Duration.ofSeconds(300))  // 요청 타임아웃 설정(5분)
                        .POST(HttpRequest.BodyPublishers.noBody())
                        .build();

                log.debug(Utils.getLogMaker(Utils.eLogType.SCRAP), htsScrapDomain);

                // 요청 보내고 응답 받기
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                log.debug(Utils.getLogMaker(Utils.eLogType.SYSTEM), "Scheduled Jsise Real Time End----------------");

                if(response.statusCode() == 200) {
                    return CustomResponse.builder()
                            .message(ErrorCode.OK.getMessage())
                            .status(response.statusCode())
                            .build();
                } else {
                    return CustomResponse.builder()
                            .message(response.toString())
                            .status(response.statusCode())
                            .build();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 실시간 시세 정보 데이터 초기화 (평일 19시)
     * 금일 저장된 데이터를 모두 삭제하고, 최신 데이터로 업데이트
     */
    @Scheduled(cron = "0 0 19 * * MON-FRI")
    public CustomResponse getScrapDeleteAndLatestJsiseRealTime() {
        if(!debugMode) {
            try {
                log.debug(Utils.getLogMaker(Utils.eLogType.SYSTEM), "Scheduled Delete Jsise Real Time Start----------------");
                // HttpClient 객체 생성
                HttpClient client = HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(300))  // 연결 타임아웃 설정(5분)
                        .build();

                // HttpRequest 객체 생성
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(htsScrapDomain + "/scrap/delete/jsise_realtime"))
                        .header("Content-Type", "application/json")
                        .timeout(Duration.ofSeconds(300))  // 요청 타임아웃 설정(5분)
                        .POST(HttpRequest.BodyPublishers.noBody())
                        .build();

                log.debug(Utils.getLogMaker(Utils.eLogType.SCRAP), htsScrapDomain);

                // 요청 보내고 응답 받기
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                log.debug(Utils.getLogMaker(Utils.eLogType.SYSTEM), "Scheduled Delete Jsise Real Time End----------------");

                if(response.statusCode() == 200) {
                    return CustomResponse.builder()
                            .message(ErrorCode.OK.getMessage())
                            .status(response.statusCode())
                            .build();
                } else {
                    return CustomResponse.builder()
                            .message(response.toString())
                            .status(response.statusCode())
                            .build();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
