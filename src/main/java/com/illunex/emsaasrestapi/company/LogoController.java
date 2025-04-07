package com.illunex.emsaasrestapi.company;

import com.illunex.emsaasrestapi.common.CustomResponse;
import com.illunex.emsaasrestapi.hts.dto.RequestHtsDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("logo")
@Slf4j
public class LogoController {

    private final LogoService logoService;

    /**
     * 종목 로고 s3 등록 유무 조회
     * @return
     */
    @PostMapping("/check")
    public CustomResponse getLogoEmptyList() {
        return logoService.getLogoEmptyList();
    }

    /**
     * 기업 로고 조회
     */
    @PatchMapping("/company")
    public CustomResponse getCompanyLogos(@RequestBody RequestHtsDTO.SearchIscds iscds) {
        return logoService.getCompanyLogos(iscds);
    }
}
