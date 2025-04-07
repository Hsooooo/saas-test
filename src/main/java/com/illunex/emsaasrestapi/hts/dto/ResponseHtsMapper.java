package com.illunex.emsaasrestapi.hts.dto;

import java.time.LocalDateTime;

public class ResponseHtsMapper {

    /**
     * 테마별 종목 등락률 DB조회용
     */
    public interface ThemeOriginalGroupInterface {
        Long getThemeIdx();
        Double getPrdyCtrt();
    }

    /**
     * 테마 테이블에서 산업분류 등락률 DB조회용
     */
    public interface ThemeOriginalIscdPrdyCtrtGroupInterface {
        String getIscd();
        Double getPrdyCtrt();
    }

    /**
     * 산업분류별 등락률 DB 조회용
     */
    public interface KsicCompanyGroupInterface {
        Long getKsicIdx();
        Double getPrdyCtrt();
    }

    /**
     * 실시간 시세 정보(10분 지연)
     */
    public interface JsiseRealTimeInerface {
        String getIscd();
        Integer getBsopDate();
        Integer getBsopHour();
        String getKorIsnm();
        Long getPrpr();
        Double getPrdyCtrt();
        Long getPrdyVrss();
        String getPrdyVrssSign();
        Long getAcmlVol();
        Long getAcmlTrPbmn();
        Long getOprc();
        Long getHgpr();
        Long getLwpr();
        Long getMxpr();
        Long getLlam();
        Long getAvls();
        Long getPrdyVol();
        Integer getFiscalYearEnd();
        Float getPer();
        Float getPbr();
        Integer getW52Hgpr();
        Integer getW52HgprDate();
        Integer getW52Lwpr();
        Integer getW52LwprDate();
        String getTrhtYn();
        LocalDateTime getCreateDate();
        LocalDateTime getUpdateDate();
    }
}
