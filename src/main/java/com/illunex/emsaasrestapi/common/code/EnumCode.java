package com.illunex.emsaasrestapi.common.code;

//import com.illunex.emsaasrestapi.common.code.entity.Code;
//import com.illunex.emsaasrestapi.common.code.repository.CodeRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class EnumCode {
//    private static LinkedHashMap<String, String> codeMap;
//    private static CodeRepository codeRepository;
//
//    public static void init(CodeRepository repository){
//        if(codeMap == null || codeMap.size() == 0){
//            codeMap = new LinkedHashMap<>();
//            codeRepository = repository;
//        }
//    }
//
//    private static void setCodeMap(){
//        if(codeMap.size() == 0) {
//            List<Code> codeList = codeRepository.findAll();
//            codeList.forEach(code -> codeMap.put(code.getCode(), code.getCodeValue()));
//        }
//    }
//
//    public static String getCodeDesc(String code){
//        setCodeMap();
//        return codeMap.get(code);
//    }
//
//    public static String[] getCodeValues(String[] codes){
//        List<String> codeList = new ArrayList<>();
//        for (String s : codes) {
//            if(!s.equals("")) {
//                codeList.add(codeMap.get(s.trim()));
//            }
//        }
//        return codeList.toArray(new String[codeList.size()]);
//    }
//
//    public static void reloadCodeMap(){
//        if(codeMap != null){
//            codeMap.clear();
//        }
//        setCodeMap();
//    }
//
//    public static class InvestorTrend {
//        // 종목별 일별 거래량 투자자 조회 구분
//        @AllArgsConstructor
//        public enum TradingVolumeSmall {
//            Investor("TVS0001", "투자자별"),
//            Institutional("TVS0002", "기관별"),
//            Empty("", "");
//
//            @Getter
//            private String code;
//            @Getter
//            private String value;
//
//            public static TradingVolumeSmall codeToEnum(String stringCode) {
//                for (TradingVolumeSmall value : values()) {
//                    if (value.code.equals(stringCode)) {
//                        return value;
//                    }
//                }
//                return null;
//            }
//        }
//
//        // 종목별 일별 거래량 조회 구분
//        @AllArgsConstructor
//        public enum TradingVolumeBig {
//            Investor("TVB0001", "투자자"),
//            MarginTrading("TVB0002", "신용"),
//            SecuritiesLending("TVB0003", "대차"),
//            ShortSelling("TVB0004", "공매도"),
//            Cfd("TVB0005", "CFD"),
//            Empty("", "");
//
//            @Getter
//            private String code;
//            @Getter
//            private String value;
//
//            public static TradingVolumeBig codeToEnum(String stringCode) {
//                for (TradingVolumeBig value : values()) {
//                    if (value.code.equals(stringCode)) {
//                        return value;
//                    }
//                }
//                return null;
//            }
//        }
//
//        // 기간별 거래량 구분
//        @AllArgsConstructor
//        public enum PeriodicTradingVolume {
//            Week("PTV0001", "주"),
//            Month("PTV0002", "월"),
//            Year("PTV0003", "년"),
//            Empty("", "");
//
//            @Getter
//            private String code;
//            @Getter
//            private String value;
//
//            public static PeriodicTradingVolume codeToEnum(String stringCode) {
//                for (PeriodicTradingVolume value : values()) {
//                    if (value.code.equals(stringCode)) {
//                        return value;
//                    }
//                }
//                return null;
//            }
//        }
//    }
//
//    public static class Finance {
//        // 재무 매출/영업이익/순이익 분기/연간 코드
//        @AllArgsConstructor
//        public enum FinanceQuarterAnnual {
//            Quarter("PQA0001", "분기"),
//            Annual("PQA0002", "연간"),
//            Empty("", "");
//
//            @Getter
//            private String code;
//            @Getter
//            private String value;
//
//            public static FinanceQuarterAnnual codeToEnum(String stringCode) {
//                for (FinanceQuarterAnnual value : values()) {
//                    if (value.code.equals(stringCode)) {
//                        return value;
//                    }
//                }
//                return null;
//            }
//        }
//
//        // 종목별 안정성 차트
//        @AllArgsConstructor
//        public enum FinanceStability {
//            DebtRatio("FSA0001", "부채비율"),
//            CurrentRatio("FSA0002", "유동비율"),
//            Empty("", "");
//
//            @Getter
//            private String code;
//            @Getter
//            private String value;
//
//            public static FinanceStability codeToEnum(String stringCode) {
//                for (FinanceStability value : values()) {
//                    if (value.code.equals(stringCode)) {
//                        return value;
//                    }
//                }
//                return null;
//            }
//        }
//    }
//
//    // AI 관련 코드
//    public static class Ai {
//        @AllArgsConstructor
//        public enum DartAnalysisCode {
//            Positive("AAC0001", "긍정"),
//            Negative("AAC0002", "부정"),
//            Neutrality("AAC0003", "중립");
//
//            @Getter
//            private String code;
//            @Getter
//            private String value;
//
//            public static Ai.DartAnalysisCode codeToEnum(String stringCode) {
//                for (Ai.DartAnalysisCode value : values()) {
//                    if (value.code.equals(stringCode)) {
//                        return value;
//                    }
//                }
//                return null;
//            }
//        }
//    }
//
//    // 회원
    public static class Member {
        @AllArgsConstructor
        public enum TypeCd {
            Normal("MTP0001", "일반회원");

            @Getter
            private String code;
            @Getter
            private String value;

            public static Member.TypeCd codeToEnum(String stringCode) {
                for (Member.TypeCd value : values()) {
                    if (value.code.equals(stringCode)) {
                        return value;
                    }
                }
                return null;
            }
        }
//
        @AllArgsConstructor
        public enum StateCd {
            Wait("MST0001", "인증대기"),
            Approval("MST0002", "인증완료"),
            Suspend("MST0003", "정지"),
            Withdrawal("MST0004", "탈퇴");

            @Getter
            private String code;
            @Getter
            private String value;

            public static Member.StateCd codeToEnum(String stringCode) {
                for (Member.StateCd value : values()) {
                    if (value.code.equals(stringCode)) {
                        return value;
                    }
                }
                return null;
            }
        }
    }
//
//    // 이메일
//    public static class Email {
//        @AllArgsConstructor
//        public enum TypeCd {
//            JoinEmail("ETP0001", "회원가입 인증"),
//            FindPasswordEmail("ETP0002", "비밀번호 찾기");
//
//            @Getter
//            private String code;
//            @Getter
//            private String value;
//
//            public static EnumCode.Email.TypeCd codeToEnum(String stringCode) {
//                for (EnumCode.Email.TypeCd value : values()) {
//                    if (value.code.equals(stringCode)) {
//                        return value;
//                    }
//                }
//                return null;
//            }
//        }
//    }
}