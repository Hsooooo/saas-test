package com.illunex.emsaasrestapi.company;

import com.illunex.emsaasrestapi.common.CustomResponse;
import com.illunex.emsaasrestapi.common.ErrorCode;
import com.illunex.emsaasrestapi.hts.dto.RequestHtsDTO;
import com.illunex.emsaasrestapi.hts.dto.ResponseHtsDTO;
import com.illunex.emsaasrestapi.company.entity.CompanyLogos;
import com.illunex.emsaasrestapi.company.repository.CompanyLogosRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.LinkedHashMap;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class LogoService {
    private final CompanyLogosRepository companyLogosRepository;
    private final ModelMapper modelMapper;

    /**
     * 종목 로고 s3 등록 유무 조회
     * @return
     */
    public CustomResponse getLogoEmptyList() {
        List<CompanyLogos> companyLogoList = companyLogosRepository.findAll();
        HttpHeaders httpHeaders = new HttpHeaders();
        RestTemplate restTemplate = new RestTemplate();

        LinkedHashMap<String, String> emptyMap = new LinkedHashMap<>();
        companyLogoList.forEach(companyLogo -> {
            UriComponents uri = UriComponentsBuilder
                    .fromHttpUrl(companyLogo.getFileUrl())
                    .build(true);
            HttpEntity<?> requestEntity = new HttpEntity<>(httpHeaders);
            try {
                restTemplate.exchange(uri.toUri(), HttpMethod.GET, requestEntity, String.class);
            } catch (Exception e) {
                String iscd = uri.getPath().substring(uri.getPath().lastIndexOf("/") + 1, uri.getPath().lastIndexOf("."));
                log.error("[Empty Image] " + companyLogo.getKorIsnm() + " : " + iscd);
                emptyMap.put(companyLogo.getKorIsnm(), iscd);
            }
        });
        return CustomResponse.builder()
                .data(emptyMap)
                .build();
    }

    /**
     * 종목별 로고 목록
     * @param iscds
     * @return
     */
    public CustomResponse getCompanyLogos(RequestHtsDTO.SearchIscds iscds) {
        List<CompanyLogos> list = companyLogosRepository.findAllByIscdIn(iscds.getIscds());
        List<ResponseHtsDTO.CompanyLogo> response = modelMapper.map(list, new TypeToken<List<ResponseHtsDTO.CompanyLogo>>(){}.getType());

        return CustomResponse.builder()
                .data(response)
                .message(ErrorCode.OK.getMessage())
                .status(ErrorCode.OK.getStatus())
                .build();
    }
}
