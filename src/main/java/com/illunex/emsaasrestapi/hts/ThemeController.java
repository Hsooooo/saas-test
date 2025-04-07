package com.illunex.emsaasrestapi.hts;

import com.illunex.emsaasrestapi.common.CustomResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("theme")
@Slf4j
public class ThemeController {

    private final ThemeService themeService;
    /**
     * 테마 카테고리 목록
     */
    @GetMapping()
    public CustomResponse getThemeList() {
        return themeService.getThemeList();
    }

    /**
     * 테마 파일 로고 목록
     */
    @GetMapping("/logo")
    public CustomResponse getThemeLogoList() {
        return themeService.getThemeLogoList();
    }

    /**
     * 테마 파일 로고 개별 조회
     */
    @GetMapping("/logo/{themeIdx}")
    public CustomResponse getThemeLogo(@PathVariable Integer themeIdx) {
        return themeService.getThemeLogo(themeIdx);
    }
}
