package com.illunex.emsaasrestapi.scrap;

import com.illunex.emsaasrestapi.common.CustomResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("scrap")
@Slf4j
public class ScrapController {

    private final ScrapAPI scrapAPI;

    /**
     * 시세/종목/투자자 동향 정보 수동 수집(당일만 가능)
     * @return
     */
    @GetMapping("/jjong_jsise")
    public CustomResponse getScrapJjongJsise() {
        return scrapAPI.getScrapJjongJsise();
    }

    /**
     * 전체 투자자 정보 수동 수집(당일만 가능)
     * @return
     */
    @GetMapping("/invest")
    public CustomResponse getScrapInvest() {
        return scrapAPI.getScrapInvest();
    }

    /**
     * 전체 재무 정보 수동 수집
     * @return
     */
    @GetMapping("/financeList")
    public CustomResponse getScrapFinanceList() {
        return scrapAPI.getScrapFinanceList();
    }

    /**
     * 개별 재무 정보 수동 수집
     * @return
     */
    @GetMapping("/finance/{iscd}")
    public CustomResponse getScrapFinance(@PathVariable String iscd) {
        return scrapAPI.getScrapFinance(iscd);
    }

    /**
     * 10분 지연 시세 정보 데이터를 최신데이터로 업데이트
     * @return
     */
    @GetMapping("/realtime")
    public CustomResponse getScrapLatestJsiseRealTime() {
        return scrapAPI.getScrapLatestJsiseRealTime();
    }

    /**
     * 10분 지연 시세 정보 데이터 삭제 후 업데이트
     * @return
     */
    @GetMapping("/delete/realtime")
    public CustomResponse getScrapDeleteLatestJsiseRealTime() { return scrapAPI.getScrapDeleteAndLatestJsiseRealTime(); }
}
