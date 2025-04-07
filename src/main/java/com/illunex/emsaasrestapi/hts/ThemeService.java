package com.illunex.emsaasrestapi.hts;

import com.illunex.emsaasrestapi.common.CustomResponse;
import com.illunex.emsaasrestapi.common.ErrorCode;
import com.illunex.emsaasrestapi.hts.dto.ResponseHtsDTO;
import com.illunex.emsaasrestapi.hts.entity.ThemeCategory;
import com.illunex.emsaasrestapi.hts.entity.ThemeLogos;
import com.illunex.emsaasrestapi.hts.repository.ThemeCategoryRepository;
import com.illunex.emsaasrestapi.hts.repository.ThemeCodeRepository;
import com.illunex.emsaasrestapi.hts.repository.ThemeLogosRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class ThemeService {
    private final ThemeCategoryRepository themeCategoryRepository;
    private final ThemeLogosRepository themeLogosRepository;
    private final ThemeCodeRepository themeCodeRepository;
    private final ModelMapper modelMapper;

    /**
     * 테마 카테고리 목록
     * @return
     */
    public CustomResponse getThemeList() {
        List<ThemeCategory> list = themeCategoryRepository.findAll();
        List<ResponseHtsDTO.ThemeCategory> response = modelMapper.map(list, new TypeToken<List<ResponseHtsDTO.ThemeCategory>>(){}.getType());

        List<String> categoryList = new ArrayList<>();
        for(int i = 0; i < list.size(); i++) {
            // 괄호 안에 콤마가 있는 테마들이 있어서 ; 로 변경처리
            String[] categories = list.get(i).getTheme_list().split(";");
            // 괄호 안에 콤마가 있기 때문에 예외처리를 위해 추가
            for(String c : categories) {
                categoryList.add(c);
            }
            response.get(i).setThemeList(categoryList);
            categoryList = new ArrayList<>();
        }
        return CustomResponse.builder()
                .message(ErrorCode.OK.getMessage())
                .status(ErrorCode.OK.getStatus())
                .data(response)
                .build();
    }


    /**
     * 테마 파일 로고 목록
     * @return
     */
    public CustomResponse getThemeLogoList() {
        List<ThemeLogos> themeLogoList = themeLogosRepository.findAll();
        return CustomResponse.builder()
                .status(ErrorCode.OK.getStatus())
                .message(ErrorCode.OK.getMessage())
                .data(modelMapper.map(themeLogoList, new TypeToken<List<ResponseHtsDTO.ThemeLogos>>(){}.getType()))
                .build();
    }


    /**
     * 테마 파일 로고 개별 조회
     * @param themeIdx
     * @return
     */
    public CustomResponse getThemeLogo(Integer themeIdx) {
        ThemeLogos themeLogos = themeLogosRepository.findByThemeIdx(themeIdx);
        return CustomResponse.builder()
                .status(ErrorCode.OK.getStatus())
                .message(ErrorCode.OK.getMessage())
                .data(modelMapper.map(themeLogos, ResponseHtsDTO.ThemeLogos.class))
                .build();
    }
}
