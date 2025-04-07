package com.illunex.emsaasrestapi.hts;

import com.illunex.emsaasrestapi.common.CustomException;
import com.illunex.emsaasrestapi.common.CustomPageRequest;
import com.illunex.emsaasrestapi.common.CustomResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("investor")
@Slf4j
public class InvestorController {

    private final InvestorService investorService;

    /**
     * 종목별 투자자 동향 상세 정보(일별)
     */
    @GetMapping("")
    public CustomResponse getIvestorTrendList(CustomPageRequest pageRequest,
                                              String[] sort,
                                              @RequestParam(name = "iscd") String iscd,
                                              @RequestParam(name = "code") String code) {
        return investorService.getIvestorTrend(pageRequest.of(sort), iscd, code);
    }

    /**
     * 종목별 투자자 동향 - 기간별 거래량 차트(6개만 보임)
     */
    @GetMapping("/chart")
    public CustomResponse getTradingVolumChart(@RequestParam(name = "iscd") String iscd,
                                               @RequestParam(name = "code") String code) throws CustomException {
        return investorService.getTradingVolumChart(iscd, code);
    }

}
