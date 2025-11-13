package com.illunex.emsaasrestapi.license;

import com.illunex.emsaasrestapi.common.CurrentMember;
import com.illunex.emsaasrestapi.common.CustomException;
import com.illunex.emsaasrestapi.common.CustomResponse;
import com.illunex.emsaasrestapi.member.vo.MemberVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("license")
@Slf4j
public class LicenseController {
    private final LicenseService licenseService;

    /**
     * 라이선스 목록 조회s
     * @return
     */
    @GetMapping("/list")
    public CustomResponse<?> getLicenses() {
        return CustomResponse.builder()
                .data(licenseService.getLicenses())
                .build();
    }

    @GetMapping("/info")
    public CustomResponse<?> getLicenseInfo(@RequestParam Integer partnershipIdx,
                                            @CurrentMember MemberVO memberVO) throws CustomException {
        return CustomResponse.builder()
                .data(licenseService.getLicenseInfo(partnershipIdx, memberVO))
                .build();
    }
}
