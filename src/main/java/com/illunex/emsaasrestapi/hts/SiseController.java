package com.illunex.emsaasrestapi.hts;

import com.illunex.emsaasrestapi.common.CustomException;
import com.illunex.emsaasrestapi.common.CustomPageRequest;
import com.illunex.emsaasrestapi.common.CustomResponse;
import com.illunex.emsaasrestapi.hts.dto.RequestHtsDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("sise")
@Slf4j
public class SiseController {

    private final SiseService siseService;

    /**
     * 종목별 상세정보 - 종목정보 탭
     * @param iscd
     * @return
     */
    @GetMapping("/{iscd}")
    public CustomResponse getJSiseDetail(@PathVariable String iscd) throws CustomException {
        return siseService.getJSiseDetail(iscd);
    }

    /**
     * 산업분류번호에 포함된 종목 시세 목록 조회
     */
    @GetMapping("/list/ksic")
    public CustomResponse getKsicJongSiseList(@RequestParam(name = "idx") Long ksicCategoryIdx,
                                              CustomPageRequest pageRequest,
                                              String[] sort) {
        return siseService.getKsicJongSiseList(ksicCategoryIdx, pageRequest.of(sort));
    }

    /**
     * 테마번호에 포함된 종목 목록 시세 조회
     */
    @GetMapping("/list/theme")
    public CustomResponse getThemeJongSiseList(@RequestParam(name = "idx") Long themeIdx,
                                               CustomPageRequest pageRequest,
                                               String[] sort) {
        return siseService.getThemeJongSiseList(themeIdx, pageRequest.of(sort));
    }

    /**
     * 종목별 일별 시세
     */
    @GetMapping()
    public CustomResponse getDailySise(CustomPageRequest pageRequest,
                                       String[] sort,
                                       @RequestParam(name = "iscd") String iscd) throws CustomException {
        return siseService.getDailySise(iscd, pageRequest.of(sort));
    }

    /**
     * 종목별 상세정보 - 차트 탭(일별 차트)
     */
    @GetMapping("/chart")
    public CustomResponse getJSiseDetailChart(CustomPageRequest pageRequest,
                                              String[] sort,
                                              @RequestParam(name = "iscd") String iscd) throws CustomException {
        return siseService.getJSiseDetailChart(iscd, pageRequest.of(sort));
    }

    /**
     * 10분 지연 산업분류별 등락률 조회
     */
    @GetMapping("/prdyCtrt/ksic")
    public CustomResponse getPrdyCtrtKsic() {
        return siseService.getPrdyCtrtKsic();
    }

    /**
     * 10분 지연 테마별 등락률 조회
     */
    @GetMapping("/prdyCtrt/theme")
    public CustomResponse getPrdyCtrtTheme() {
        return siseService.getPrdyCtrtTheme();
    }

    /**
     * 10분지연 시세 조회
     */
    @PatchMapping("/prdyCtrt/realTime")
    public CustomResponse getPrdyCtrtRealTime(@RequestBody RequestHtsDTO.SearchIscds searchIscds) {
        return siseService.getPrdyCtrtRealTime(searchIscds);
    }

    /**
     * 10분 지연 TOP 100 시세 조회
     */
    @GetMapping("/top/realTime")
    public CustomResponse getTopRealTime(CustomPageRequest pageRequest,
                                         String[] sort) {
        return siseService.getTopRealTime(pageRequest.of(sort));
    }

    /**
     * 스톡 페이지 사이드 패널(시세 정보)
     */
    @GetMapping("/stock/{iscd}")
    public CustomResponse getStockSiseInfo(@PathVariable String iscd) {
        return siseService.getStockSiseInfo(iscd);
    }

    /**
     * 스톡 페이지 종목별 차트
     */
    @PatchMapping("/stock/chart")
    public CustomResponse getStockChart(@RequestBody RequestHtsDTO.SearchStockChart search) throws CustomException {
        return siseService.getStockChart(search);
    }
}
