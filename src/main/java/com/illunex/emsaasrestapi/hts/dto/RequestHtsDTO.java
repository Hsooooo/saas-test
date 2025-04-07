package com.illunex.emsaasrestapi.hts.dto;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class RequestHtsDTO {
    /**
     * 로그인용
     */
    @Getter
    @Setter
    @Builder
    public static class HTSLoginInfo {
        private String id;
        private String pw;
        private String privateIp;
        private String macAddress;
    }

    /**
     * 종목별 투자자 동향(일별)
     */
    @Getter
    @Setter
    public static class InvestorTrend {
        private String iscd;
        private String code; // investor: 투자자별, institutional: 기관별
    }

    /**
     * 종목별 상세정보 - 차트 탭
     */
    @Getter
    public static class JstockJongChart {
        private String iscd;
        private Long chartCnt; // 차트 가져올 개수
        private Long volumeCnt; // 거래량 가져올 개수
    }

    /**
     * 종목 목록 로고/개별 실시간 시세 조회용
     */
    @Getter
    public static class SearchIscds {
        private List<String> iscds;
    }

    /**
     * 스톡 페이지 차트 조회
     */
    @Getter
    public static class SearchStockChart {
        private String iscd; // 종목번호
        private Integer from; // 조회 기준 날짜
        private int cnt; // 가져올 개수
    }
}
