package com.illunex.emsaasrestapi.hts.dto;

import com.illunex.emsaasrestapi.common.code.EnumCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ResponseHtsDTO {

    /**
     * 채권 시세
     */
    @Getter
    @Setter
    public static class BSise {
        // 단축 종목코드
        private String iscd;
        // 표준종목코드
        private String stndIscd;
        // 현재가
        private int prpr;
        // 한글 종목명
        private String korIsnm;
        // 전일 대비
        private int prdyVrss;
        // 전일 대비 부호 (1:상한 2:상승 3:보합 4:하한 5:하락)
        private String prdyVrssSign;
        // 전일 대비율 (단위 : %)
        private double prdyCtrt;
        // 시간 (단위 : HHMMSS)
        private int bsopHour;
        // 일자 (단위 : YYYYMMDD)
        private int bsopDate;
        // 전일 종가
        private int prdyClpr;
        // 누적 거래량 (단위 : 주)
        private long acmlVol;
        // 누적 거래 대금 (단위 : 원)
        private long acmlTrPbmn;
        // 시가
        private int oprc;
        // 고가
        private int hgpr;
        // 저가
        private int lwpr;
        // 전일 거래량 (단위 : 주)
        private long prdyVol;
        // 채권 체결 수익률
        private double bondCntgErt;
    }

    /**
     * CME(야간선물)종목 정보
     */
    @Getter
    @Setter
    public static class CmeJong {
        // 단축 종목코드
        private String iscd;
        // 표준종목코드
        private String stndIscd;
        // 한글 종목명
        private String korIsnm;
        // 기준가
        private int sdpr;
        // 월물 구분 코드 (1:최근월물 2:2째월물 3:3째월물 ...)
        private String mmscClsCode;
        // 실시간가격제한여부
        private String dynmcPrcLmtYn;
    }

    /**
     * ELW 종목 정보
     */
    @Getter
    @Setter
    public static class ElwJong {
        // 단축 종목코드
        private String iscd;
        // 한글 종목명
        private String korIsnm;
        // 기초자산코드
        private String unasStndIscd;
        // 기초자산코드2
        private String unasStndIscd2;
        // 기초자산코드3
        private String unasStndIscd3;
        // 기초자산코드4
        private String unasStndIscd4;
        // 기초자산코드5
        private String unasStndIscd5;
        // 발행회사명
        private String pblcCoName;
        // 발행회사코드
        private String elwPblcMrktPrttNo;
        // 행사가
        private double acpr;
        // 최종거래일
        private int lastBsopDate;
        // 잔존 일수
        private short htsRmnnDynu;
        // 권리유형구분코드
        private String rghtTypeClsCode;
        // 표준종목코드
        private String stndIscd;
        // ELW권리형태
        private String elwNvltOptnClsCode;
        // LP종료일자
        private int lpFinDate;
    }

    /**
     * EUREX 종목 정보
     */
    @Getter
    @Setter
    public static class EuJong {
        // 단축 종목코드
        private String iscd;
        // 표준종목코드
        private String stndIscd;
        // 한글 종목명
        private String korIsnm;
        // ATM구분코드
        private String atmClsCode;
        // 거래승수
        private double trMltl;
        // 월물 구분 코드 (1:최근월물 2:2째월물 3:3째월물 ...)
        private String mmscClsCode;
        // 실시간가격제한여부
        private String dynmcPrcLmtYn;
    }

    /**
     * 해외선물종목 정보
     */
    @Getter
    @Setter
    public static class FfJong {
        // 종목코드(내부)
        private String prdtCd;
        // 시세종목코드
        private String qttnPrdtCd;
        // 한글종목명
        private String odrvPrdtNm;
        // 품목코드
        private String prlsCd;
        // 해외파생품목명
        private String odrvPrlsNm;
        // 행사가격
        private double optExcsPrcCtns2;
        // 국가코드
        private String ntnCd;
        // 거래소코드
        private String excgCd;
        // 품목유형구분명
        private String prlsTpDcdNm;
        // 가격소수점정보
        private int prcDcpnBlwLngCtns;
        // 진법
        private int indcPrcCtns2;
        // TICK SIZE
        private double tckPrcCtns2;
        // 최소가격변동금액
        private double tckValuAmtCtns2;
        // 환산승수
        private double trslMltCtns2;
        // 계약단위, 계약크기
        private double ctrtUnAmtCtns2;
        // 위탁증거금
        private double csgnWmyCtns2;
        // 유지증거금
        private double mnmgCtns3;
        // 상장일자
        private int lstDt;
        // 최종거래일
        private int lastTrDt;
        // 만기일자
        private int expDt;
        // 만기년월
        private int expYm;
        // 결제통화(거래통화)
        private String crryCd;
        // 표시통화
        private String pflsStlCrryCd;
        // 근월물순서
        private int nmctOrdCtns;
        // 잔존일수
        private int srvlDnumCtns2;
        // Active Flag(종목상태구분코드)
        private String prdtTrDcd;
        // 기준가
        private double excalPrcCtns2;
        // 전일종가
        private double bfdtClprCtns2;
        // 전일거래량
        private long bfdtTrQntCtns2;
        // 거래가능 여부
        private String prlsTrPmssYn;
        // 시작시간
        private int mropTm;
        // 종료시간
        private int mrndTm;
        // 장시작시간(한국)
        private int acplOpngTm;
        // 장종료시간(한국)
        private int acplEndTm;
        // 직전거래일
        private int excalDt;
        // 현영업일, 거래일자
        private int trDt;
        // 상장후고가
        private double lochigh;
        // 상장후저가
        private double loclow;
    }

    /**
     * 선물 시세
     */
    @Getter
    @Setter
    public static class FSise {
        // 단축종목코드
        private String iscd;
        // 표준종목코드
        private String stndIscd;
        // 현재가
        private double prpr;
        // 한글 종목명
        private String korIsnm;
        // 영문 종목명
        private String engIsnm;
        // 전일 대비
        private double prdyVrss;
        // 전일 대비 부호 (1:상한 2:상승 3:보합 4:하한 5:하락)
        private String prdyVrssSign;
        // 전일 대비율 (단위 : %)
        private double prdyCtrt;
        // 시간 (단위 : HHMMSS)
        private int bsopHour;
        // 일자 (단위 : YYYYMMDD)
        private int bsopDate;
        // 시가
        private double oprc;
        // 고가
        private double hgpr;
        // 저가
        private double lwpr;
        // 누적 거래량 (단위 : 주)
        private long acmlVol;
        // 누적 거래 대금 (단위 : 원)
        private long acmlTrPbmn;
        // 기준가
        private double sdpr;
        // 상한가
        private double mxpr;
        // 하한가
        private double llam;
        // 전일 종가
        private double prdyClpr;
        // 매수호가1
        private double bidp1;
        // 매수호가2
        private double bidp2;
        // 매수호가3
        private double bidp3;
        // 매수호가4
        private double bidp4;
        // 매수호가5
        private double bidp5;
        // 매수호가6
        private double bidp6;
        // 매수호가7
        private double bidp7;
        // 매수호가8
        private double bidp8;
        // 매수호가9
        private double bidp9;
        // 매수호가10
        private double bidp10;
        // 매도호가1
        private double askp1;
        // 매도호가2
        private double askp2;
        // 매도호가3
        private double askp3;
        // 매도호가4
        private double askp4;
        // 매도호가5
        private double askp5;
        // 매도호가6
        private double askp6;
        // 매도호가7
        private double askp7;
        // 매도호가8
        private double askp8;
        // 매도호가9
        private double askp9;
        // 매도호가10
        private double askp10;
        // 매도호가잔량1
        private long askpRsqn1;
        // 매도호가잔량2
        private long askpRsqn2;
        // 매도호가잔량3
        private long askpRsqn3;
        // 매도호가잔량4
        private long askpRsqn4;
        // 매도호가잔량5
        private long askpRsqn5;
        // 매도호가잔량6
        private long askpRsqn6;
        // 매도호가잔량7
        private long askpRsqn7;
        // 매도호가잔량8
        private long askpRsqn8;
        // 매도호가잔량9
        private long askpRsqn9;
        // 매도호가잔량10
        private long askpRsqn10;
        // 매수호가잔량1
        private long bidpRsqn1;
        // 매수호가잔량2
        private long bidpRsqn2;
        // 매수호가잔량3
        private long bidpRsqn3;
        // 매수호가잔량4
        private long bidpRsqn4;
        // 매수호가잔량5
        private long bidpRsqn5;
        // 매수호가잔량6
        private long bidpRsqn6;
        // 매수호가잔량7
        private long bidpRsqn7;
        // 매수호가잔량8
        private long bidpRsqn8;
        // 매수호가잔량9
        private long bidpRsqn9;
        // 매수호가잔량10
        private long bidpRsqn10;
        // 선물옵션직전가
        private double rgbfPrc;
        // 현물기초자산가격
        private double unasPrc;
        // 전일정산가
        private double prdyExclPrc;
        // 배당금액지수미래가치금액
        private double dmixFutrWrthVal;
        // CD금리
        private double cdMnrt;
        // 상장 일자
        private int lstnDate;
        // 상장 폐지 일자
        private int lstnAbrgDate;
        // 스프레드 기준 종목 유형 코드 (F:원월 N:근월)
        private String speadStndIssuTypeCode;
        // 결제 방법 구분 코드 (C:현금결제 D:실물인수도결제)
        private String stlmMthdClsCode;
        // 기초자산ID (K2I:KOSPI200)
        private String unasClsCode;
        // 스프레드 유형 코드 (T1:최근월+차근월 T2:최근월+2차근월)
        private String speadTypeCode;
        // 선물 최종 거래 일자
        private int lastTrDate;
        // 선물 최종 결제 일자
        private int lastStlmDate;
        // 월물 구분 코드 (1:최근월물 2:2째월물 3:3째월물 ...)
        private String mmscClsCode;
        // 만기년월
        private String mtrtYymm;
        // 조정 구분 코드 (C:거래단위조정 N:조정없음 O:미결제조정)
        private String adjsClsCode;
        // 선물 거래 단위
        private double trUnit;
        // 거래 승수
        private double trMltl;
        // 시장 조성 여부
        private String mrktPrmgYn;
        // 상장 유형 코드
        private String lstnTypeClsCode;
        // 조정사유코드
        /*
        00:해당사항없음 01:권리락 02:배당락
        03:분배락, 04:권배락
        */
        private String adjsReasCode;
        // 기초자산 단축코드
        private String unasShrnIscd;
        // 기초자산 표준종목코드
        private String unasStndIscd;
        // 잔존 일수
        private short htsRmnnDynu;
        // 조정기준가격
        private double adjsSdpr;
        /*
        기준 가격 구분 코드
        11:전일정산가
        12:전일기준가(거래성립전 종가미형성)
        13:당일이론가(거래성립전 종가미형성)
        14:전일기세(거래성립전 기세형성)
        15:당일이론가(거래성립전 기세형성)
        16:조정된전일정산가
        17:조정된전일기준가(거래성립전 종가미형성)
        18:조정된전일기세(거래성립전 기세형성)
        19:전일대상자산 종가(이론가없는 상품)
        21:전일증거금기준가
        22:전일기준가(거래성립전 종가미형성)
        23:당일이론가(거래성립전 종가미형성)
        24:전일기세(거래성립전 기세
        */
        private String stndPrcClsCode;
        /*
        매매 기준 가격 구분 코드
        0:해당없음
        1:실세
        2:기세
        3:이론가
        4:대상자산종가
        */
        private String dealStndPrcClsCode;
        // 전일조정종가
        private double prdyAdjsClpr;
        // 협의 거래 대상 여부
        private String dscsTrTrgtYn;
        // 전일 증거금 기준 가격
        private double prdyMargStndPrc;
        // 정산 이론가
        private double exclThpr;
        // 기준 이론가
        private double stndThpr;
        // 거래정지 여부
        private String trhtYn;
        // 서킷브레이커 적용 상한가
        private double crbrAplyMxpr;
        // 서킷브레이커 적용 하한가
        private double crbrAplyLlam;
        // ATM구분코드 (0:선물, 1:ATM, 2:ITM, 3:OTM)
        private String atmClsCode;
        // 최종 거래 일 여부
        private String lastTrDayYn;
        // 전일 종가 구분 코드 (1:실세, 2:기세, 3:거래무)
        private String prdyClprClsCode;
        // 전일시가
        private double prdyOprc;
        // 전일고가
        private double prdyHgpr;
        // 전일저가
        private double prdyLwpr;
        // 전일 정산 가격 구분 코드
        private String prdyExclPrcClsCode;
        // 정산가격 정산이론가 괴리율
        private double exclPrcThprDprt;
        // 전일 미결제 약정 수량
        private long htsPrdyOtstStplQty;
        // 전일매도우선호가가격
        private double prdyAskp;
        // 전일매수우선호가가격
        private double prdyBidp;
        // 전일 체결 건수
        private int prdyCntgCsnu;
        // 전일 누적거래량
        private long prdyCnqn;
        // 전일 누적거래대금
        private long prdyCntgAmt;
        // 전일협의대량매매체결수량
        private long prdyDscsBltrCntgQty;
        // 전일협의대량매매거래대금
        private long prdyDscsBltrTrPbmn;
        // 미결제 한도 계약수
        private int otstLimtCnrcQty;
        // 소속 상품군
        private String blngPrgpCode;
        // 상품군 옵셋율
        private double prgpOfstRate;
        // 지정가 호가 조건 구분 코드
        private String lmtsAsprCondClsCode;
        // 시장가 호가 조건 구분 코드
        private String mrprAsprCondClsCode;
        // 조건부지정가 호가 조건 구분 코드
        private String cnlmAsprCondClsCode;
        // 최유리지정가 호가 조건 구분 코드
        private String bslpAsprCondClsCode;
        // EFP  거래대상여부
        private String efpTrgtYn;
        // FLEX 거래대상여부
        private String flexTrgtYn;
        // EFP  전일체결수량
        private long efpPrdyCntgQty;
        // EFP  전일거래대금
        private long efpPrdyTrPbmn;
        // 내재변동성
        private double intsVltl;
    }

    /**
     * 지수선물종목
     */
    @Getter
    @Setter
    public static class FStockJong {
        // 단축 종목코드
        private String iscd;
        // 표준종목코드
        private String stndIscd;
        // 한글 종목명
        private String korIsnm;
        // 기준가
        private int sdpr;
        // 월물 구분 코드 (1:최근월물 2:2째월물 3:3째월물 ...)
        private String mmscClsCode;
        // 실시간가격제한여부
        private String dynmcPrcLmtYn;
    }

    /**
     * FX 마진 종목 정보
     */
    @Getter
    @Setter
    public static class FxJong {
        // 해외종목코드
        private String scrnIndcPrdtCd;
        // 통화코드1
        private String trStdrCrryCd;
        // 통화코드2
        private String trOpntCrryCd;
        // Quote Mode
        private String fxQttnRcvgMthDcd;
        // 가격소수점
        private double prcDcpnBlwLngCtns;
        // 가격소수점보정
        private double tckPrcCtns2;
        // 고객소수점
        private double scrnPrcDcpnBlwLngCtns;
        // 고객소수점보정
        private double scrnTckPrcCtns;
        // 딜링룸코드
        private String fdmUnqNo;
        // PL계산통화
        private String pflsClcPrdtCd;
        // PL계산방법
        private String fxPflsClcDcd;
        // 대표업종한글종목명
        private String pairIdFcmCd;
        // FX 상품명
        private String odrvPrdtNm;
        // 매입 이자율
        private double buyIntrRtCtns;
        // 매도 이자율
        private double sellIntrRtCtns;
        // FDM 매입 이자율
        private double fdmBuyIntrRtCtns;
        // FDM 매도 이자율
        private double fdmSellIntrRtCtns;
    }

    /**
     * 관심 조회
     */
    @Getter
    @Setter
    public static class InterSise {
        // 단축 종목코드
        private String iscd;
        // 표준종목코드
        private String stndIscd;
        // 한글 종목명
        private String korIsnm;
        // 현재가
        private double prpr;
        // 전일 대비
        private double prdyVrss;
        // 전일 대비 부호 (1:상한 2:상승 3:보합 4:하한 5:하락)
        private String prdyVrssSign;
        // 전일 대비율 (단위 : %)
        private double prdyCtrt;
        // 시간 (단위 : HHMMSS)
        private int bsopHour;
        // 일자 (단위 : YYYYMMDD)
        private int bsopDate;
        // 전일 종가
        private double prdyClpr;
        // 누적 거래량 (단위 : 주)
        private long acmlVol;
        // 누적 거래 대금 (단위 : 원)
        private long acmlTrPbmn;
        // 시가
        private double oprc;
        // 고가
        private double hgpr;
        // 저가
        private double lwpr;
        // 상한가
        private double mxpr;
        // 하한가
        private double llam;
        // 시가총액
        private long avls;
        // 전일 거래량 (단위 : 주)
        private long prdyVol;
        // 매도호가1
        private double askp1;
        // 매도호가2
        private double askp2;
        // 매도호가3
        private double askp3;
        // 매도호가4
        private double askp4;
        // 매도호가5
        private double askp5;
        // 매도호가6
        private double askp6;
        // 매도호가7
        private double askp7;
        // 매도호가8
        private double askp8;
        // 매도호가9
        private double askp9;
        // 매도호가10
        private double askp10;
        // 매수호가1
        private double bidp1;
        // 매수호가2
        private double bidp2;
        // 매수호가3
        private double bidp3;
        // 매수호가4
        private double bidp4;
        // 매수호가5
        private double bidp5;
        // 매수호가6
        private double bidp6;
        // 매수호가7
        private double bidp7;
        // 매수호가8
        private double bidp8;
        // 매수호가9
        private double bidp9;
        // 매수호가10
        private double bidp10;
        // 매도호가 잔량1
        private long askpRsqn1;
        // 매도호가 잔량2
        private long askpRsqn2;
        // 매도호가 잔량3
        private long askpRsqn3;
        // 매도호가 잔량4
        private long askpRsqn4;
        // 매도호가 잔량5
        private long askpRsqn5;
        // 매도호가 잔량6
        private long askpRsqn6;
        // 매도호가 잔량7
        private long askpRsqn7;
        // 매도호가 잔량8
        private long askpRsqn8;
        // 매도호가 잔량9
        private long askpRsqn9;
        // 매도호가 잔량10
        private long askpRsqn10;
        // 매수호가 잔량1
        private long bidpRsqn1;
        // 매수호가 잔량2
        private long bidpRsqn2;
        // 매수호가 잔량3
        private long bidpRsqn3;
        // 매수호가 잔량4
        private long bidpRsqn4;
        // 매수호가 잔량5
        private long bidpRsqn5;
        // 매수호가 잔량6
        private long bidpRsqn6;
        // 매수호가 잔량7
        private long bidpRsqn7;
        // 매수호가 잔량8
        private long bidpRsqn8;
        // 매수호가 잔량9
        private long bidpRsqn9;
        // 매수호가 잔량10
        private long bidpRsqn10;
        // 52주 최고가
        private long w52Hgpr;
        // 52주 최고가 일자 (단위 : YYYYMMDD)
        private long w52HgprDate;
        // 52주 최저가
        private long w52Lwpr;
        // 52주 최저가 일자 (단위 : YYYYMMDD)
        private long w52LwprDate;
        // 액면가
        private double fcam;
        // 대용가
        private long sspr;
        // 기준가
        private long sdpr;
        // 상장 주수 (단위 : 주)
        private long lstnStcn;
        /*
        신 장운영 구분 코드
        첫번째비트-'0': 예상 1:장개시전 2:장중 3:장종료후 4:시간외단일가
        두번째비트-0:보통 1:종가 2:대량 3:바스켓   '7':정리매매
         */
        private String newMkopClsCode;
        // NOT_NULL 데이터 종류 (0: 지수, 1: 종목)
        private String dataCode;
        // 국가구분코드
        private String ntnlClsCode;
        // 통화코드
        private String crncCode;
        // 주가 INDICATOR(DENOMINATOR)
        private double xdiv;
        // 거래일자(한국일자. 체결데이터)
        private int korBsopDate;
        // 거래시간(한국시간. 체결데이터)
        private int korBsopHour;
    }

    /**
     * 투자자 정보
     */
    @Getter
    @Setter
    public static class Invest {
        private int pos;
        // NOT_NULL 시장 분류 구분 코드
        private String mrktDivClsCode;
        // NOT_NULL 업종 구분 코드
        private String bstpClsCode;
        // NOT_NULL 그룹 구분 코드
        private String grpClsCode;
        // NOT_NULL 상품 번호
        private String prodNo;
        // 주식 영업 일자
        private int bsopDate;
        // 현재 시간
        private int bsopHour;
        // 업종 지수 현재가
        private double prprNmix;
        // 전일 대비 부호
        private String prdyVrssSign;
        // 전일 대비
        private double bstpNmixPrdyVrss;
        // 증권 매도 거래량 금융투자
        private long scrtSelnVol;
        // 증권 매수 거래량 금융투자
        private long scrtShnuVol;
        // 증권 매도 거래대금 금융투자
        private long scrtSelnTrPbmn;
        // 증권 매수 거래대금 금융투자
        private long scrtShnuTrPbmn;
        // 보험 매도 거래량
        private long insuSelnVol;
        // 보험 매수 거래량
        private long insuShnuVol;
        // 보험 매도 거래대금
        private long insuSelnTrPbmn;
        // 보험 매수 거래대금
        private long insuShnuTrPbmn;
        // 투신 매도 거래량
        private long ivtrSelnVol;
        // 투신 매수 거래량
        private long ivtrShnuVol;
        // 투신 매도 거래대금
        private long ivtrSelnTrPbmn;
        // 투신 매수 거래대금
        private long ivtrShnuTrPbmn;
        // 사모펀드 매도 거래량
        private long peFundSelnVol;
        // 사모펀드 매수 거래량
        private long peFundShnuVol;
        // 사모펀드 매도 거래대금
        private long peFundSelnTrPbmn;
        // 사모펀드 매수 거래대금
        private long peFundShnuTrPbmn;
        // 은행 매도 거래량
        private long bankSelnVol;
        // 은행 매수 거래량
        private long bankShnuVol;
        // 은행 매도 거래대금
        private long bankSelnTrPbmn;
        // 은행 매수 거래대금
        private long bankShnuTrPbmn;
        // 종금 매도 거래량 기타금융
        private long mrbnSelnVol;
        // 종금 매수 거래량 기타금융
        private long mrbnShnuVol;
        // 종금 매도 거래대금 기타금융
        private long mrbnSelnTrPbmn;
        // 종금 매수 거래대금 기타금융
        private long mrbnShnuTrPbmn;
        // 기금 매도 거래량 연기금
        private long fundSelnVol;
        // 기금 매수 거래량 연기금
        private long fundShnuVol;
        // 기금 매도 거래대금 연기금
        private long fundSelnTrPbmn;
        // 기금 매수 거래대금 연기금
        private long fundShnuTrPbmn;
        // 기타 단체 매도 거래량 국가자치
        private long etcOrgtSelnVol;
        // 기타 단체 매수 거래량 국가자치
        private long etcOrgtShnuVol;
        // 기타 단체 매도 거래대금 국가자치
        private long etcOrgtSelnTrPbmn;
        // 기타 단체 매수 거래대금 국가자치
        private long etcOrgtShnuTrPbmn;
        // 기타 법인 매도 거래량
        private long etcCorpSelnVol;
        // 기타 법인 매수 거래량
        private long etcCorpShnuVol;
        // 기타 법인 매도 거래대금
        private long etcCorpSelnTrPbmn;
        // 기타 법인 매수 거래대금
        private long etcCorpShnuTrPbmn;
        // 기타 매도 거래량
        private long etcSelnVol;
        // 기타 매수 거래량
        private long etcShnuVol;
        // 기타 매도 거래대금
        private long etcSelnTrPbmn;
        // 기타 매수 거래대금
        private long etcShnuTrPbmn;
        // 개인 매도 거래량
        private long prsnSelnVol;
        // 개인 매수 거래량
        private long prsnShnuVol;
        // 개인 매도 거래대금
        private long prsnSelnTrPbmn;
        // 개인 매수 거래대금
        private long prsnShnuTrPbmn;
        // 외국인 등록 매도 거래량 외국인
        private long frgnRegSelnVol;
        // 외국인 등록 매수 거래량 외국인
        private long frgnRegShnuVol;
        // 외국인 등록 매도 거래대금 외국인
        private long frgnRegSelnTrPbmn;
        // 외국인 등록 매수 거래대금 외국인
        private long frgnRegShnuTrPbmn;
        // 외국인 비등록 매도 거래량 기타외인
        private long frgnNregSelnVol;
        // 외국인 비등록 매수 거래량 기타외인
        private long frgnNregShnuVol;
        // 외국인 비등록 매도 거래대금 기타외인
        private long frgnNregSelnTrPbmn;
        // 외국인 비등록 매수 거래대금 기타외인
        private long frgnNregShnuTrPbmn;
        // 외국인 매도 거래량 외국인계
        private long frgnSelnVol;
        // 외국인 매수 거래량 외국인계
        private long frgnShnuVol;
        // 외국인 매도 거래대금 외국인계
        private long frgnSelnTrPbmn;
        // 외국인 매수 거래대금 외국인계
        private long frgnShnuTrPbmn;
        // 기관계 매도 거래량
        private long orgnSelnVol;
        // 기관계 매수 거래량
        private long orgnShnuVol;
        // 기관계 매도 거래대금
        private long orgnSelnTrPbmn;
        // 기관계 매수 거래대금
        private long orgnShnuTrPbmn;
        // 전체 매도 거래량
        private long wholSelnVol;
        // 전체 매수 거래량
        private long wholShnuVol;
        // 전체 매도 거래대금
        private long wholSelnTrPbmn;
        // 전체 매수 거래대금
        private long wholShnuTrPbmn;
    }

    /**
     * 주식선물종목 정보
     */
    @Getter
    @Setter
    public static class JFStockJong {
        // 단축 종목코드
        private String iscd;
        // 표준종목코드
        private String stndIscd;
        // 한글 종목명
        private String korIsnm;
        // ATM구분코드
        private String atmClsCode;
        // 기초자산코드
        private String unasStndIscd;
        // 거래승수
        private double trMltl;
        // 기준가
        private int sdpr;
        // 실시간가격제한여부
        private String dynmcPrcLmtYn;
    }

    /**
     * 주식옵션종목 정보
     */
    @Getter
    @Setter
    public static class JOStockJong {
        // 단축 종목코드
        private char    iscd;
        // 표준종목코드
        private char    stndIscd;
        // 기초자산명
        private char    unasIsnm;
        // 한글 종목명
        private char    korIsnm;
        // 콜풋구분(2:콜, 3:풋)
        private char    optnClsCode;
        // 만기년월
        private char    mtrtYymm;
        // 행사가
        private double  acpr;
        // ATM구분코드
        private char    atmClsCode;
        // 거래승수
        private double  trMltl;
        // 실시간가격제한여부
        private char    dynmcPrcLmtYn;
    }

    /**
     * 주식 시세
     */
    @Setter
    @Getter
    public static class JSise {
        private Long idx;
        // 단축 종목코드
        private String iscd;
        // 표준종목코드
        private String stndIscd;
        // 현재가
        private int prpr;
        // 한글 종목명
        private String korIsnm;
        // 전일 대비
        private int prdyVrss;
        // 전일 대비 부호(1:상한, 2:상승, 3:보합, 4:하한, 5:하락
        private String prdyVrssSign;
        // 전일 대비율(단위 : %)
        private double prdyCtrt;
        // 시간(단위 : HHMMSS)
        private int bsopHour;
        // 일자(단위 : YYYYMMDD)
        private int bsopDate;
        // 전일 종가(단위 : 원)
        private long prdyClpr;
        // 누적 거래량(단위 : 주)
        private long acmlVol;
        // 누적 거래 대금(단위 : 원)
        private long acmlTrPbmn;
        // 시가
        private int oprc;
        // 고가
        private int hgpr;
        // 저가
        private int lwpr;
        // 상한가
        private int mxpr;
        // 하한가
        private int llam;
        // 시가총액(단위 : 원)
        private long avls;
        // 전일거래량(단위 : 주)
        private long prdyVol;
        // 매도호가1
        private int askp1;
        // 매도호가2
        private int askp2;
        // 매도호가3
        private int askp3;
        // 매도호가4
        private int askp4;
        // 매도호가5
        private int askp5;
        // 매도호가6
        private int askp6;
        // 매도호가7
        private int askp7;
        // 매도호가8
        private int askp8;
        // 매도호가9
        private int askp9;
        // 매도호가10
        private int askp10;
        // 매수호가1
        private int bidp1;
        // 매수호가2
        private int bidp2;
        // 매수호가3
        private int bidp3;
        // 매수호가4
        private int bidp4;
        // 매수호가5
        private int bidp5;
        // 매수호가6
        private int bidp6;
        // 매수호가7
        private int bidp7;
        // 매수호가8
        private int bidp8;
        // 매수호가9
        private int bidp9;
        // 매수호가10
        private int bidp10;
        // 매도호가 잔량1
        private long askpRsqn1;
        // 매도호가 잔량2
        private long askpRsqn2;
        // 매도호가 잔량3
        private long askpRsqn3;
        // 매도호가 잔량4
        private long askpRsqn4;
        // 매도호가 잔량5
        private long askpRsqn5;
        // 매도호가 잔량6
        private long askpRsqn6;
        // 매도호가 잔량7
        private long askpRsqn7;
        // 매도호가 잔량8
        private long askpRsqn8;
        // 매도호가 잔량9
        private long askpRsqn9;
        // 매도호가 잔량10
        private long askpRsqn10;
        // 매수호가 잔량1
        private long bidpRsqn1;
        // 매수호가 잔량2
        private long bidpRsqn2;
        // 매수호가 잔량3
        private long bidpRsqn3;
        // 매수호가 잔량4
        private long bidpRsqn4;
        // 매수호가 잔량5
        private long bidpRsqn5;
        // 매수호가 잔량6
        private long bidpRsqn6;
        // 매수호가 잔량7
        private long bidpRsqn7;
        // 매수호가 잔량8
        private long bidpRsqn8;
        // 매수호가 잔량9
        private long bidpRsqn9;
        // 매수호가 잔량10
        private long bidpRsqn10;
        // 52주 최고가
        private int w52Hgpr;
        // 52주 최고가 일자
        private int w52HgprDate;
        // 52주 최저가
        private int w52Lwpr;
        // 52주 최저가 일자
        private int w52LwprDate;
        // 액면가
        private double fcam;
        // 대용가
        private int sspr;
        // 기준가
        private int sdpr;
        // 상장 주수(단위 : 주)
        private int lstnStcn;
        // 신 장운영 구분 코드(첫번째비트 - 0:예상, 1:장개시전, 2:장중, 3:장종료후, 4:시간외단일가 / 두번째비트 - 0:보통, 1:종가, 2:대량, 3:바스켓, 7:정리매매)
        private String newMkopClsCode;
        // 외국인보유비율
        private double frgnHldnRate;
        // 영문종목명
        private String engIsnm;
        // 락구분코드
        private String flngClsCode;
        // 액면가변경구분코드
        private String fcamModClsCode;
        // 시가기준가종목여부
        private String oprcSdprIssuYn;
        // 재평가종목사유코드
        private String revlIssuReasCode;
        // 기준가변경종목여부
        private String sdprModIssuYn;
        // 시장경고구분코드
        private String mrktAlrmClsCode;
        // 관리종목여부
        private String mangIssuYn;
        // 불성실공시여부
        private String insnPbntYn;
        // 우회상장여부
        private String bypsLstnYn;
        // 거래정지여부
        private String trhtYn;
        // 업종대분류코드
        private String bstpLargDivCode;
        // 업종중분류코드
        private String bstpMedmDivCode;
        // 업종소분류코드
        private String bstpSmalDivCode;
        // KOSPI200 채용구분코드
        private String kospi200ApntClsCode;
        // KRX100 종목여부
        private String krx100IssuYn;
        // KOSPI 종목여부
        private String kospiIssuYn;
        // KOSPI100 종목여부
        private String kospi100IssuYn;
        // KOSPI50 종목여부
        private String kospi50IssuYn;
        // 전일종가구분코드
        private String prdyClsCode;
        // 평가가격
        private int htsStckVltnPrc;
        // 발행가격
        private int stckPblcPrc;
        // 배당수익률
        private double dvdnErt;
        // 행사가
        private double acpr;
        // 자본금
        private double cpfn;
        // 신용주문가능여부
        private String crdtOderAbleYn;
        // 지정호가조건구분코드
        private String lmtsAsprCondClsCode;
        // 시장가호가조건구분코드
        private String mrprAsprCondClsCode;
        // 조건부지정가호가조건구분코드
        private String cnlmAsprCondClsCode;
        // 최유리지정가호가조건구분코드
        private String bslpAsprCondClsCode;
        // 최우선지정가호가조건구분코드
        private String pmprLmtsAsprCondClsCode;
        // 증자구분코드
        private String icicClsCode;
        // 우선주구분코드
        private String prstClsCode;
        // 국민주여부
        private String ntstYn;
        // 주식 최저 호가 가격
        private int stckLwstAsprPrc;
        // 주식 최고 호가 가격
        private int stckHghsAsprPrc;
        // 정규시장매매수량단위
        private int frmlMrktDealQtyUnit;
        // 시간외시장매매수량단위
        private int ovtmMrktDealQtyUnit;
        // 리츠구분코드
        private String stckReitClsCode;
        // 목적표준종목코드
        private String objtStndIscd;
        // LP주문가능여부
        private String lpOderAbleYn;
        // 장개시전시간외종가가능여부
        private String mkonBefOvtmClprAbleYn;
        // 장개시전시간외대량매매가능여부
        private String mkonBefOvtmBltrAbleYn;
        // 장개시전시간외바스켓가능여부
        private String mkonBefOvtmBsktAbleYn;
        // RegulationS적용종목여부
        private String reglSYn;
        // 기업인수목적회사여부
        private String etprUndtObjtCoYn;
        // ETF 유통주수
        private int etfCrclStcn;
        // 전일과표기기준고가격
        private double prdyStasStndPrc;
        // 전일배당전과표기준가격
        private double prdyDvdnBefStasStndPrc;
        // 전일현금배당금액
        private double prdyCashDvdnAmt;
        // 전전일과표기준가격
        private double d2BefStasStndProc;
        // 대용가사정비율
        private double ssprAssmRate;
        // 투자주의환기종목여부
        private String warnYn;
        // 과세유형
        private String txtnTypeCode;
        private LocalDateTime createDate;
        private LocalDateTime updateDate;
    }

    /**
     * 종목별 상세정보(종목 정보 탭)
     */
    @Getter
    @Setter
    public static class JstockJongDetail {
        private ResponseHtsDTO.JSise jSise; // 종목 시세
        private ResponseHtsDTO.CompanyInfo companyInfo; // 기업 정보
        private ResponseHtsDTO.JjongInvestor investorTrend; // 투자자 동향
        private List<ResponseHtsDTO.ThemeCode> themeCodeList; // 테마 정보
        private ResponseHtsDTO.InvestmentIndicators investmentIndicators; // 투자 지표
        private ResponseHtsDTO.JStockJong jStockJong; // 주식종목 정보
    }

    /**
     * 종목별 상세정보(차트 탭)
     */
    @Getter
    @Setter
    public static class JSiseChartDetail {
        private ResponseHtsDTO.CompanyInfo companyInfo; // 기업정보
        private List<ResponseHtsDTO.JSise> chartList; // 주식 차트
        private List<ResponseHtsDTO.Board> boardList; // 토론 게시판
    }

    /**
     * 종목 투자자 동향(종목 선택 - 종목정보 탭 선택)
     */
    @Getter
    @Setter
    public static class JjongInvestor {
        private Long individualNetBuyVolume; //개인 순매수(수량)
        private Long individualBuyVolume; //개인매수(수량)
        private Long individualSellVolume; //개인 매도(수량)

        private Long netForeignBuyingVolume;          // 외국인 순매수 수량
        private Long foreignBuyVolume;          // 외국인 매수(수량)
        private Long foreignSellVolume;          // 외국인 매도(수량)

        private Long institutionalNetBuyVolume; // 기관계 순매수(수량)
        private Long institutionalBuyVolume; // 기관계 매수(수량)
        private Long institutionalSellVolume; // 기관계 매도(수량)
    }

    /**
     * 기업 정보
     */
    @Getter
    @Setter
    public static class CompanyInfo {
        private Integer id;
        // 사업자등록번호
        private String bizNum;
        // 법인등록번호
        private String corporationNum;
        // 기업명
        private String companyName;
        // 순수 기업명
        private String realCompanyName;
        // 법인 상태
        private String companyState;
        // 대표자명
        private String representationName;
        // 기업유형
        private String companyType;
        // 기업 규모
        private String companySize;
        // 직원수
        private String employeeCount;
        // 설립일
        private String establishmentDate;
        // 재무일자
        private String acctMonth;
        // 산업코드 업태(산업코드 앞 1자리 알파벳)
        private String businessConditionCode;
        // 산업코드 업태명
        private String businessConditionDesc;
        // 산업코드
        private String businessCategoryCode;
        // 산업코드명
        private String businessCategoryDesc;
        // 홈페이지 주소
        private String homepage;
        // 전화번호
        private String tel;
        // 대표 이메일
        private String email;
        // 팩스
        private String fax;
        // 주소
        private String address;
        // 우편번호
        private String zipCode;
        // 매출(천원)
        private String sales;
        // 매출년도
        private String salesYear;
        // 대표제품
        private String majorProduct;
        // 본사여부
        private String headOffice;
        // 카테고리
        private String category;
        // 키워드
        private String keyword;
        // 공개유무(1: 공개, 0: 비공개)
        private boolean visible;
        // 소개
        private String description;
        // 0: 바우처, 1: ked_detail, 2: 대전
        private int originId;
        // 상장시장구분코드(1: 상장, 2: 코스닥, 3: 코넥스, 4: K-OTC, 9: 기타)
        private String listingMarketId;
        // 상장코드명
        private String listingMarketDesc;
        // 기업국가
        private String country;
        // 로고 URL
        private String logoUrl;
        // 일루넥스 번호
        private String illuId;
        // 코스콤 수집 성공여부(0: 수집성공, 1: 수집실패, 2: 직접입력)
        private int isKoscomScrapSuccess;
        // 표준종목코드
        private String stndIscd;
        // 단축종목코드
        private String iscd;
        private LocalDate createDate;
        private LocalDate updateDate;
    }

    /**
     * 종목 (상장사)
     */
    @Getter
    @Setter
    public static class JStockJong {
        // 표준종목코드
        private String stndIscd;
        // 단축 종목코드
        private String iscd;
        // 한글 종목명
        private String korIsnm;
        // 시장 분류 구분 코드 (시장 분류 구분 코드(1:코스피,2:코스닥,A:코스피(파생),P:코넥스))
        private String mrktDivClsCode;
        // 증권그룹구분코드
        private String scrtGrpClsCode;
        // 시가총액 규모 구분코드
        private String avlsScalClsCode;
        // 업종대분류코드
        private String bstpLargDivCode;
        // 업종중분류코드
        private String bstpMedmDivCode;
        // 업종소분류코드
        private String bstpSmalDivCode;
        // 재조업구분코드
        private String mninClsCode;
        // 배당지수종목여부
        private String dvdnNmixIssuYn;
        // 지배구조우량여부
        private String sprnDtrrSprrYn;
        // KOSPI200채용구분코드
        private String kospi200ApntClsCode;
        // KOSPI100종목여부
        private String kospi100IssuYn;
        // 지배구조지수종목여부
        private String sprnStrrNmixIssuYn;
        // KRX100종목여부
        private String krx100IssuYn;
        // 결산월
        private String stacMonth;
        // 액면가
        private float stckFcam;
        // 주식 기준가
        private int stckSdpr;
        // 자본금
        private double lstnCpf;
        // 상장주수
        private long lstnStcn;
        // 배당수익율
        private double dvdnErt;
        // 신용잔고비율
        private float crdtRmndRate;
        // 거래정지여부(Y:정상, N:거래정지)
        private String trhtYn;
        // 정리매매여부(Y:정상, N:정리매매)
        private String sltrYn;
        // 관리 종목 여부
        private String mangIssuYn;
        // 시장 경고 구분 코드
        private String mrktAlrmClsCode;
        // 시장 경고 예고
        private String mrktAlrmRiskAdntYn;
        // 불성실공시여부
        private String insnPbntYn;
        // 우회상장여부
        private String bypsLstnYn;
        // 락구분코드
        private String flngClsCode;
        // 당사신용비율
        private float crdtRate;
        // 고위험종목 여부
        private String highRisk;
        // Equity Rating A,B,C,D,F
        private String equRating;
        // 시가총액 (단위 : 원)
        private long avls;
        // 외국인보유비율
        private double frgnHldnRate;
        // 우선주구분코드
        private String prstClsCode;
    }

    /**
     * 지수옵션 종목 정보
     */
    @Getter
    @Setter
    public static class OStockJong {
        // 단축 종목코드
        private String iscd;
        // 표준종목코드
        private String stndIscd;
        // 한글 종목명
        private String korIsnm;
        // ATM구분코드
        private String atmClsCode;
        // 거래승수
        private double trMltl;
        // 월물 구분 코드 (1:최근월물 2:2째월물 3:3째월물 ...)
        private String mmscClsCode;
        // 실시간가격제한여부
        private String dynmcPrcLmtYn;
    }

    /**
     * 해외 시세 정보
     */
    @Getter
    @Setter
    public static class Ovrs {
        private int pos;
        // NOT_NULL 심볼
        private String iscd;
        // 현재가 999.9999
        private double  prpr;
        // 한글명
        private String korIsnm;
        // 전일대비 999.9999
        private double prdyVrss;
        // 전일대비구분 (1:상한 2:상승 3:보합 4:하한 5:하락)
        private String prdyVrssSign;
        // 등락률
        private double prdyCtrt;
        // 거래일자(현지일자. 체결데이터)
        private int bsopDate;
        // 거래시간(현지시간. 체결데이터)
        private int bsopHour;
        // 거래일자(한국일자. 체결데이터)
        private int korBsopDate;
        // 거래시간(한국시간. 체결데이터)
        private int korBsopHour;
        // 전일 종가 6.2
        private double prdyClpr;
        // 누적거래량
        private long acmlVol;
        // 시가 999.9999
        private double ovrsOprc;
        // 고가 999.9999
        private double ovrsHgpr;
        // 저가 999.9999
        private double ovrsLwpr;
        // 상한가
        private double ovrsMxpr;
        // 하한가
        private double ovrsLlam;
        // 매수호가
        private double bidp;
        // 매도호가
        private double askp;
        // 매수잔량
        private long bidpRsqn;
        // 매도잔량
        private long askpRsqn;
        // 52주일 최고가
        private double w52Hgpr;
        // 52주일 최고가 일자
        private int w52HgprDate;
        // 52주일 최저가
        private double w52Lwpr;
        // 52주일 최저가 일자
        private int w52LwprDate;
        // 액면가
        private double fcvlPrc;
        // 상장주식수
        private double lstnStcn;
        // NOT_NULL 데이터 종류 (0 : 지수 * 1 : 종목)
        private String dataCode;
        // 국가구분코드
        private String ntnlClsCode;
        // 통화코드
        private String crncCode;
        // 주가 INDICATOR(DENOMINATOR)
        private double xdiv;
        // 마켓
        private String mrkt;
        // 영문명
        private String engIsnm;
        // ISIN CODE
        private String isinCode;
        // 거래 상태
        private String trStat;
        // 매도량 단위
        private long selnVolUnit;
        // 매수량 단위
        private long shnuVolUnit;
        // 액면가 통화코드
        private String fcvlPrcCurrCode;
    }

    /**
     * 해외지수 정보
     */
    @Getter
    @Setter
    public static class OvrsIndex {
        private int pos;
        // NOT_NULL 심볼
        private String iscd;
        // 한글종목명
        private String korIsnm;
        // 현재가
        private double prpr;
        // 전일대비
        private double prdyVrss;
        // 전일대비부호
        private String prdyVrssSign;
        // 등락률
        private double prdyCtrt;
        // 시간
        private int bsopHour;
        // 일자
        private int bsopDate;
        // 거래량
        private long vol;
        // 시가
        private double oprc;
        // 고가
        private double hprc;
        // 저가
        private double lprc;
        /*
        데이터종류 구분자
        TO-BE
        0: index
        1: stock
        2: future
        3: option
        4: forex
        5: Multex
        7: others

        AS-IS
        P: 미국지수
        Q: 미국종목(다우30, 나스닥100, S&P500 종목만 전송)
        D: 미국상장국내기업(ADR)
        C: 상품선물
        F: CME 선물
        G: 유럽상장국내기업(GDR)
        X: 환율
        R: 금리  (없음)
        L: 리보금리
        B: 주요국정부채
        N: 국내금리
        M: 반도체 (없음)
        H: 세계주요종목
        E: ECN
        W: 세계주가지수
        */
        private String dataCode;
        // 국가구분코드
        private String ntnlClsCode;
        // 영문종목명
        private String engIsnm;

        /* 아래 안쓰는것 같아서 일단 주석처리함.
        private String bstpClsCode;          // 업종구분코드
        private String dow30Yn;              // 다우30 편입종목여부    0:미편입 1:편입 - 미국종목(Q)에 해당함
        private String nasdaq100Yn;          // 나스닥100 편입종목여부 0:미편입 1:편입 - 미국종목(Q)에 해당함
        private String snp500Yn;             // S&P 500  편입종목여부  0:미편입 1:편입 - 미국종목(Q)에 해당함
        private String exchClsCode;          // 거래소코드
        */
    }

    /**
     * 전일대비
     */
    @Getter
    @Setter
    public static class SimpleFid {
        // 단축 종목코드
        private String iscd;
        // 표준종목코드
        private String stndIscd;
        // 현재가
        private int prpr;
        // 한글 종목명
        private String korIsnm;
        // 전일 대비
        private int prdyVrss;
        // 전일 대비 부호 (1:상한 2:상승 3:보합 4:하한 5:하락)
        private String prdyVrssSign;
        // 전일 대비율 (단위 : %)
        private double prdyCtrt;
    }

    /**
     * 계좌 정보
     */
    @Getter
    @Setter
    public static class UserAccn {
        // 종합계좌대체번호
        private String ctno;
        // 종합계좌번호
        private String cano;
        // 계좌상품번호
        private String apno;
        // 고객명
        private String cusnm;
        // 가상계좌여부
        private String vrtAcntYn;
        // 가상계좌비밀번호여부
        private String vrtAcntPwdYn;
        // 삼풍계좌폐쇄구분코드
        private String prdtAcntClsgDcd;
        // 주문대리인등록여부
        private String ordrAngtRgstYn;
        // 미수발생열부
        private String rcvbOccYn;
    }

    /**
     * 유저 정보
     */
    @Getter
    @Setter
    public static class UserOut {
        // User ID
        private String usid;
        // 인증서번호
        private String ctftNo;
        // 인증서일련번호
        private String ctftSn;
        // 접속여부
        private String cnnTn;
        // 인증서비밀번호 오류횟수
        private String ctfcPwdErrCnt;
        // 고객구분코드
        private String custDcd;
        // 보안카드종류
        private String scrdKnd;
        // 서비스 구분
        private String srvcKnd;
        // 약정등록구분
        private String ctrcRgstDvsn;
        // 전자금융해지사유구분코드
        private String elcFnnCclcRsn;
        // 고객실명확인번호
        private String custRnmCnfmNo;
        // 고객식별번호
        private String cpin;
        // 고객명
        private String custNm;
        // 최종접속일자
        private String lastCnnDt;
        // 최종접속시간
        private String lastCnnHr;
        // 시세전용여부
        private String qttnDvrYn;
        // 2일전일자
        private String bf2Dt;
        // 이전일자
        private String dfDt;
        // 당일일자
        private String thdtDt;
        // 익일자
        private String nxDt;
        // 2일익일자
        private String nx2Dt;
        // 접속번호
        private String cnnNo;
        // 계좌정보 개수
        private String accNCnt;
        // 유저 계좌 정보
        private UserAccn userAccn;
    }

    /**
     * 업종 시세
     */
    @Getter
    @Setter
    public static class USise {
        // 업종 구분 코드
        private String bstpClsCode;
        // 업종 지수 구분 코드 (1: KOSPI, 2: KOSPI200, 3: KRX, 4: KOSDAQ, 5: 프리보드)
        private String nmixClsCode;
        // 현재가
        private double prpr;
        // 한글 종목명
        private String korIsnm;
        // 전일 대비
        private double prdyVrss;
        // 전일 대비 부호(1:상한 2:상승 3:보합 4:하한 5:하락)
        private String prdyVrssSign;
        // 전일 대비율 (단위 : %)
        private double prdyCtrt;
        // 일자 (단위 : YYYYMMDD)
        private int bsopDate;
        // 시간 (단위 : HHMMSS)
        private int bsopHour;
        // 전일 종가
        private double prdyClpr;
        // 누적 거래량 (단위 : 주)
        private long acmlVol;
        // 누적 거래 대금 (단위 : 원)
        private long acmlTrPbmn;
        // 시가
        private double oprc;
        // 고가
        private double hgpr;
        // 저가
        private double lwpr;
        // 상장 주수 (단위 : 주)
        private int lstnStcn;
        // 업종 지수 최고가 시간 (단위 : HHMMSS)
        private int hgprHour;
        // 업종 지수 최저가 시간 (단위 : HHMMSS)
        private int lwprHour;
        // 상승 종목 수
        private int ascnIssuCnt;
        // 상한 종목 수
        private int uplmIssuCnt;
        // 하락 종목 수
        private int downIssuCnt;
        // 하한 종목 수
        private int lslmIssuCnt;
        // 보합 종목 수
        private int stnrIssuCnt;
        // 거래 형성 종목 수
        private int trFrmtIssuCnt;
        // 기세 종목 수
        private int qtqtIssuCnt;
        // 상장 자본금
        private double lstnCpfn;
        // 시가총액 (주식현재가*상장주수)
        private long htsAvls;
        // 상장 종목 수
        private int lstnIssuCnt;
        // 상장 회사 수
        private int lstnCoCnt;
        // 가중 평균 주식 가격
        private double wghnAvrgStckPrc;
    }

    /**
     * 종목별 투자자 동향 정보
     */
    @Getter
    @Setter
    public static class InvestorTrend {
        private Integer idx;
        private String iscd;                          /* 종목 코드*/
        private String investorDate;                 /* 투자자 동향 날짜*/
        private long netForeignBuyingVolume;         /* 외국인 순매수 수량 */
        private long foreignBuyVolume;         /* 외국인 매수(수량)  */
        private long foreignSellVolume;         /* 외국인 매도(수량)  */
        private long foreignNetBuyAmount;/*외국인 순매수(금액)*/
        private long foreignBuyAmount;/*외국인 매수(금액)  */
        private long foreignSellAmount;/*외국인 매도(금액)  */
        private long individualNetBuyVolume;/*개인 순매수(수량) */
        private long individualBuyVolume;/*개인매수(수량) */
        private long individualSellVolume;/*개인 매도(수량) */
        private long individualNetBuyAmount;/* 개인 순매수(금액) */
        private long individualBuyAmount;/* 개인 매수(금액) */
        private long individualSellAmount;/* 개인 매도(금액) */
        private long institutionalNetBuyVolume;/* 기관계 순매수(수량) */
        private long institutionalBuyVolume;/* 기관계 매수(수량) */
        private long institutionalSellVolume;/* 기관계 매도(수량) */
        private long institutionalNetBuyAmount;/* 기관계 순매수(금액) */
        private long institutionalBuyAmount;/* 기관계 매수(금액) */
        private long institutionalSellAmount;/* 기관계 매도(금액) */
        private long investmentTrustNetBuyVolume;/* 투신 순매수(수량) */
        private long investmentTrustBuyVolume;/* 투신 매수(수량) */
        private long investmentTrustSellVolume;/* 투신 매도(수량) */
        private long investmentTrustNetBuyAmount;/* 투신 순매수(금액) */
        private long investmentTrustBuyAmount;/* 투신 매수(금액) */
        private long investmentTrustSellAmount;/* 투신 매도(금액) */
        private long privateEquityNetBuyVolume;/* 사모펀드 순매수(수량) */
        private long privateEquityBuyVolume;/* 사모펀드 매수(수량) */
        private long privateEquitySellVolume;/* 사모펀드 매도(수량) */
        private long privateEquityNetBuyAmount;/* 사모펀드 순매수(금액) */
        private long privateEquityBuyAmount;/* 사모펀드 매수(금액) */
        private long privateEquitySellAmount;/* 사모펀드 매도(금액) */
        private long financialInvestmentNetBuyVolume;/* 금융투자 순매수(수량) */
        private long financialInvestmentBuyVolume;/* 금융투자 매수(수량) */
        private long financialInvestmentSellVolume;/* 금융투자 매도(수량) */
        private long financialInvestmentNetBuyAmount;/* 금융투자 순매수(금액) */
        private long financialInvestmentBuyAmount;/* 금융투자 매수(금액) */
        private long financialInvestmentSellAmount;/* 금융투자 매도(금액) */
        private long insuranceNetBuyVolume;/* 보험 순매수(수량) */
        private long insuranceBuyVolume;/* 보험 매수(수량) */
        private long insuranceSellVolume;/* 보험 매도(수량) */
        private long insuranceNetBuyAmount;/* 보험 순매수(금액) */
        private long insuranceBuyAmount;/* 보험 매수(금액) */
        private long insuranceSellAmount;/* 보험 매도(금액) */
        private long bankNetBuyVolume;/* 은행 순매수(수량) */
        private long bankBuyVolume;/* 은행 매수(수량) */
        private long bankSellVolume;/* 은행 매도(수량) */
        private long bankNetBuyAmount;/* 은행 순매수(금액) */
        private long bankBuyAmount;/* 은행 매수(금액) */
        private long bankSellAmount;/* 은행 매도(금액) */
        private long otherFinancialNetBuyVolume;/* 기타금융 순매수(수량) */
        private long otherFinancialBuyVolume;/* 기타금융 매수(수량) */
        private long otherFinancialSellVolume;/* 기타금융 매도(수량) */
        private long otherFinancialNetBuyAmount;/* 기타금융 순매수(금액) */
        private long otherFinancialBuyAmount;/* 기타금융 매수(금액) */
        private long otherFinancialSellAmount;/* 기타금융 매도(금액) */
        private long pensionFundsNetBuyVolume;/* 연기금 등 순매수(수량) */
        private long pensionFundsBuyVolume;/* 연기금 등 매수(수량) */
        private long pensionFundsSellVolume;/* 연기금 등 매도(수량) */
        private long pensionFundsNetBuyAmount;/* 연기금 등 순매수(금액) */
        private long pensionFundsBuyAmount;/* 연기금 등 매수(금액) */
        private long pensionFundsSellAmount;/* 연기금 등 매도(금액) */
        private long otherCorporationsNetBuyVolume;/* 기타법인 순매수(수량) */
        private long otherCorporationsBuyVolume;/* 기타법인 매수(수량) */
        private long otherCorporationsSellVolume;/* 기타법인 매도(수량) */
        private long otherCorporationsNetBuyAmount;/* 기타법인 순매수(금액) */
        private long otherCorporationsBuyAmount;/* 기타법인 매수(금액) */
        private long otherCorporationsSellAmount;/* 기타법인 매도(금액) */
        private long domesticAndForeignNetBuyVolume;/* 내외국인 순매수(수량) */
        private long domesticAndForeignBuyVolume;/* 내외국인 매수(수량) */
        private long domesticAndForeignSellVolume;/* 내외국인 매도(수량) */
        private long domesticAndForeignNetBuyAmount;/* 내외국인 순매수(금액) */
        private long domesticAndForeignBuyAmount;/* 내외국인 매수(금액) */
        private long domesticAndForeignSellAmount;/* 내외국인 매도(금액) */
        private LocalDate createDate;
        private LocalDate updateDate;
    }

    /**
     * 종목별 투자자 동향 거래량 상세 페이지(투자자별)
     */
    @Getter
    @Setter
    public static class Investor {
        private String investorDate; // 날짜
        private Long individualNetBuyAmount; // 개인 순매수(금액)
        private Long individualNetBuyVolume; // 개인 순매수(수량)
        private Long foreignNetBuyAmount; // 외국인 순매수(금액)
        private Long netForeignBuyingVolume; // 외국인 순매수(수량)
        private Long institutionalNetBuyVolume; // 기관계 순매수(수량)
        private Long institutionalNetBuyAmount; // 기관계 순매수(금액)
        private Long otherCorporationsNetBuyVolume; // 기타법인 순매수(수량)
        private Long otherCorporationsNetBuyAmount; // 기타법인 순매수(금액)
    }

    /**
     * 종목별 투자자 동향 거래량 상세 페이지(기관별)
     */
    @Getter
    @Setter
    public static class Institutional {
        private String investorDate; // 날짜
        private Long insuranceNetBuyVolume; // 보험 순매수(수량)
        private Long insuranceNetBuyAmount; // 보험 순매수(금액)
        private Long financialInvestmentNetBuyVolume; // 금융투자 순매수(수량)
        private Long financialInvestmentNetBuyAmount; // 금융투자 순매수(금액)
        private Long otherFinancialNetBuyVolume; // 기타금융 순매수(수량)
        private Long otherFinancialNetBuyAmount; // 기타금융 순매수(금액)
        private Long investmentTrustNetBuyVolume; // 투신 순매수(수량)
        private Long investmentTrustNetBuyAmount; // 투신 순매수(금액)
        private Long privateEquityNetBuyVolume; // 사모펀드 순매수(수량)
        private Long privateEquityNetBuyAmount; // 사모펀드 순매수(금액)
        private Long pensionFundsNetBuyVolume; // 연기금 등 순매수(수량)
        private Long pensionFundsNetBuyAmount; // 연기금 등 순매수(금액)
        private Long bankNetBuyVolume; // 은행 순매수(수량)
        private Long bankNetBuyAmount; // 은행 순매수(금액)
    }

    /**
     * 테마 코드
     */
    @Getter
    @Setter
    public static class ThemeCode {
        private Long idx;
        private String themeName; // 테마명
        private Double prdyCtrt; // 전일 대비 등락률(%)
        private LocalDateTime scrapDate; // 수집 날짜
    }

    /**
     * 종목별 상세정보 - 투자 지표
     */
    @Getter
    @Setter
    public static class InvestmentIndicators {
        private Long marketCap; // 시가 총액
        private Float dividendYield; // 배당 수익률
        private Float pbr; // pbr
        private Float per; // per
        private Float roe; // roe
        private Float psr; // psr
        private Double foreignOwnershipRatio; // 외국인 소진율
        private Integer fiscalYearEnd; // 결산년월
        private LocalDateTime createDate;
    }

    /**
     * 토론
     */
    @Getter
    @Setter
    public static class Board {
        private Long idx;       // 게시판 번호
        private String title;   // 제목
        private String content; // 내용
        private String nickName; // 닉네임
        private String profileUrl; // 프로필 사진 url
        private String profilePath; // 프로필 사진 경로
        private LocalDateTime createDate; // 생성일
        private LocalDateTime updateDate; // 수정일
    }

    /**
     * 종목별 투자자 동향 - 기간별 거래량 차트(6개만 리턴시킴)
     */
    @Getter
    @Setter
    public static class TradingVolumChart {
        private Long foreignVolume = 0L; // 외국인 수량
        private Long individualVolume = 0L; // 개인 수량
        private Long institutionalVolume = 0L; // 기관 수량
        private LocalDate startDate; // 시작날짜
        private LocalDate endDate; // 종료날짜
        private String date; // 날짜(주/월/년)
        private String unitCd; // 날짜 단위(주: PTV0001, 월: PTV0002, 년: PTV0003)
        private String unitDesc;
        public void setUnitCd(String code) {
            this.unitCd = code;
            this.unitDesc = EnumCode.getCodeDesc(code);
        }
    }

    @Getter
    @Setter
    public static class WeekNumber {
        private int weekNumber;
        private LocalDate now;
    }

    @Getter
    @Setter
    public static class CompanyLogo {
        private Long idx;
        private String iscd;                        // 종목번호
        private String korIsnm; // 한글 종목명
        private String fileUrl; // 로고 url
        private String filePath; // 로고 위치
        private String fileName; // 파일 원본이름
        private String fileAlias; // 파일 별칭(종목번호_한글종목명)
        private Integer fileSize; // 파일크기
        private LocalDateTime update_date; // 수정일
        private LocalDateTime createDate; // 등록일
    }

    /**
     * 테마 카테고리 목록
     */
    @Getter
    @Setter
    public static class ThemeCategory {
        private Long idx;
        private String theme_name; // 테마명
        private List<String> themeList; // 테마목록
        private LocalDateTime createDate;
        private LocalDateTime updateDate;
    }

    /**
     * 테마 로고 파일
     */
    @Getter
    @Setter
    public static class ThemeLogos {
        private Long idx; // 테마 로고 idx
        private Long themeIdx; // 테마 idx
        private String fileUrl; // 테마 로고 URL
        private String filePath; // 로고 위치
        private String fileName; // 파일 원본이름
        private String fileAlias; // 파일 별칭(테마idx_테마명)
        private Integer fileSize; // 파일크기
        private LocalDateTime update_date; // 수정일
        private LocalDateTime createDate; // 등록일
    }

    /**
     * 테마별 종목 등락률 DB조회용
     */
    @Getter
    public static class ThemeOriginalGroup {
        private Long themeIdx;
        private Double prdyCtrt;

        public ThemeOriginalGroup(ResponseHtsMapper.ThemeOriginalGroupInterface entity) {
            this.themeIdx = entity.getThemeIdx();
            this.prdyCtrt = entity.getPrdyCtrt();
        }
    }

    @Getter
    @Setter
    public static class JsiseChart {
        private Long idx;
        private String iscd; // 종목코드
        private Integer prpr; // 현재가
        private Integer oprc; // 시가
        private Integer hgpr; // 고가
        private Integer lwpr; // 저가
        private Integer mxpr; // 상한가
        private Integer llam; // 하한가
        private Integer bsopDate; // 일자(단위 : YYYYMMDD)
        // 누적 거래량(단위 : 주)
        private long acmlVol;

        // 전일 대비율(단위 : %)
        private Double prdyCtrt;

        public JsiseChart(Long idx, String iscd, Integer bsopDate, Integer prpr, Integer oprc, Integer hgpr, Integer lwpr, Integer mxpr, Integer llam, long acmlVol, Double prdyCtrt) {
            this.idx = idx;
            this.iscd = iscd.replaceAll(" ", "");
            this.bsopDate = bsopDate;
            this.prpr = prpr;
            this.oprc = oprc;
            this.hgpr = hgpr;
            this.lwpr = lwpr;
            this.mxpr = mxpr;
            this.llam = llam;
            this.acmlVol = acmlVol;
            this.prdyCtrt = prdyCtrt;
        }
    }

    /**
     * 테마 테이블에서 산업분류 등락률 DB조회용
     */
    @Getter
    public static class ThemeOriginalIscdPrdyCtrt {
        private String iscd;
        private Double prdyCtrt;

        public ThemeOriginalIscdPrdyCtrt(ResponseHtsMapper.ThemeOriginalIscdPrdyCtrtGroupInterface entity) {
            this.iscd = entity.getIscd();
            this.prdyCtrt = entity.getPrdyCtrt();
        }
    }

    /**
     * 테마 테이블에서 산업분류 등락률 DB조회용
     */
    @Getter
    public static class KsicCompanyGroup {
        private Long ksicIdx;
        private Double prdyCtrt;

        public KsicCompanyGroup(ResponseHtsMapper.KsicCompanyGroupInterface entity) {
            this.ksicIdx = entity.getKsicIdx();
            this.prdyCtrt = entity.getPrdyCtrt();
        }
    }

    /**
     * 산업분류별 등락률
     */
    @Getter
    @Setter
    public static class KsicCategory {
        private Long ksicCategoryIdx; // 산업분류 idx
        private String companyBusinessCategoryCode; // 기업 산업 코드
        private String ksicCode; // 산업분류코드
        private String ksicDesc; // 산업분류코드명
        private Double prdyCtrt; // 산업분류코드별 전일대비 등락률(%)
        private LocalDateTime scrapDate; // 수집날짜
        private LocalDateTime updateDate;
        private LocalDateTime createDate;
    }

    /**
     * 실시간 시세 정보(10분 지연)
     */
    @Getter
    @Setter
    public static class JsiseRealTime {
        // 종목번호
        private String iscd;
        // 일자 (단위 : YYYYMMDD)
        private Integer bsopDate;
        // 일자 (단위 : YYYYMMDD)
        private Integer bsopHour;
        // 한글 종목명
        private String korIsnm;
        // 현재가
        private Long prpr;
        // 전일대비율(단위: %)
        private Double prdyCtrt;
        // 전일 대비
        private Long prdyVrss;
        // 전일 대비 부호(1:상한 2:상승 3:보합 4:하한 5:하락)
        private String prdyVrssSign;
        // 누적 거래량(단위: 주)
        private Long acmlVol;
        // 누적 거래 대금 (단위 : 원)
        private Long acmlTrPbmn;
        // 시가
        private Long oprc;
        // 고가
        private Long hgpr;
        // 저가
        private Long lwpr;
        // 상한가
        private Long mxpr;
        // 하한가
        private Long llam;
        // 시가총액(단위: 원)
        private Long avls;
        // 전일거래량 (단위: 주)
        private Long prdyVol;
        // 결산년월
        private Integer fiscalYearEnd;
        private Float per;
        private Float pbr;
        // 52주 최고가
        private Integer w52Hgpr;
        // 52주 최고가 일자
        private Integer w52HgprDate;
        // 52주 최저가
        private Integer w52Lwpr;
        // 52주 최저가 일자
        private Integer w52LwprDate;
        // 거래정지여부
        private String trhtYn;
        private LocalDateTime createDate;
        private LocalDateTime updateDate;
        public JsiseRealTime() {}
        public JsiseRealTime(ResponseHtsMapper.JsiseRealTimeInerface entity) {
            this.iscd = entity.getIscd();
            this.bsopDate = entity.getBsopDate();
            this.bsopHour = entity.getBsopHour();
            this.korIsnm = entity.getKorIsnm();
            this.prpr = entity.getPrpr();
            this.prdyCtrt = entity.getPrdyCtrt();
            this.prdyVrss = entity.getPrdyVrss();
            this.prdyVrssSign = entity.getPrdyVrssSign();
            this.acmlVol = entity.getAcmlVol();
            this.acmlTrPbmn = entity.getAcmlTrPbmn();
            this.oprc = entity.getOprc();
            this.hgpr = entity.getHgpr();
            this.lwpr = entity.getLwpr();
            this.mxpr = entity.getMxpr();
            this.llam = entity.getLlam();
            this.avls = entity.getAvls();
            this.prdyVol = entity.getPrdyVol();
            this.fiscalYearEnd = entity.getFiscalYearEnd();
            this.per = entity.getPer();
            this.pbr = entity.getPbr();
            this.w52Hgpr = entity.getW52Hgpr();
            this.w52HgprDate = entity.getW52HgprDate();
            this.w52Lwpr = entity.getW52Lwpr();
            this.w52LwprDate = entity.getW52LwprDate();
            this.trhtYn = entity.getTrhtYn();
            this.createDate = entity.getCreateDate();
            this.updateDate = entity.getUpdateDate();
        }
    }

    /**
     * 스톡 페이지 사이드 패널(시세 정보)
     */
    @Getter
    @Setter
    public static class StockSiseInfo {
        private String iscd; // 종목코드
        private String korIsnm; // 한국종목명
        private Integer prpr; // 현재가
        private Integer bsopDate; // 현재가 일자(단위 : YYYYMMDD)
        private Integer bsopHour; // 현재가 시간 (단위 : HHMMSS)
        private Long acmlVol; // 누적 거래량(단위 : 주)
        private Long avls; // 시가총액(단위: 원)
        private Double prdyCtrt; // 전일대비 등락률
        private Long prdyVrss; // 전일 대비(원)
        private Double dayCtrt; // 전일 등락률
        private Integer dayBsopDate; // 전일 등락률 일자(단위 : YYYYMMDD)
        private Integer dayBsopHour; // 전일 등락률 시간 (단위 : HHMMSS)
        private Double weekCtrt; // 주별 등락률
        private Integer weekBsopDate; // 주별 등락률 일자(단위 : YYYYMMDD)
        private Integer weekBsopHour; // 주별 등락률 시간 (단위 : HHMMSS)
        private Double monthCtrt; // 월별 등락률
        private Integer monthBsopDate; // 월별 등락률 일자(단위 : YYYYMMDD)
        private Integer monthBsopHour; // 월별 등락률 시간 (단위 : HHMMSS)
        private Double yearCtrt; // 년별 등락률
        private Integer yearBsopDate; // 년별 등락률 일자(단위 : YYYYMMDD)
        private Integer yearBsopHour; // 년별 등락률 시간 (단위 : HHMMSS)
        private Long lstnStcn; // 상장 주수(단위 : 주)
        private String mrktDivClsCode; // 시장 분류 구분 코드(1:코스피,2:코스닥,A:코스피(파생),P:코넥스)
        private Float stckFcam; // 액면가(원)
        // 외국인 소진율이 데이터에 없고, 직접 계산하려 했지만 외국인 보유 주식 수가 없음.
        private Double foreignOwnershipRatio; // 외국인 소진율 (외국인 매도 주식 수 / 외국인 보유 주식 수) * 100
        private Float dividendYield; // 배당 수익률
        private Long avlsRanking; // 시가 총액 순위
        private String consolidated; // 연결구분코드(1: 연결재무, 2: 별도재무)
        private Integer fiscalYearEnd;                        // 결산년월
        private Float eps;
        private Float pbr;
        private Integer bps;
        private Float per;
        private Double psr; // 주가매출비율(시가총액/매출액)
        private Float roe;
        // pes 현재 데이터에 없음.
        private Float pes; // 주가수익비율(주가 / 주당 순이익(eps))
        // 매수, 매도의 경우 외국인/개인/기관계/투신/사모펀드/금융투자/보험/은행/기타금융/연기금/기타법인/내외국인을 다 더한 수량임
        private Long buyVolume; // 매수 수량(단위: 주)
        private Long sellVolume; // 매도 수량(단위: 주)
    }

    /**
     * 스톡페이지 종목별 차트 조회
     */
    @Getter
    @Setter
    public static class StockChartGroup {
        private Long idx;
        private String iscd;
        // 일자(단위 : YYYYMMDD)
        private Integer bsopDate;
        // 시가
        private Integer oprc;
        // 고가
        private Integer hgpr;
        // 저가
        private Integer lwpr;
        // 현재가
        private Integer prpr;
        // 전일 종가(단위 : 원)
        private Long prdyClpr;
        // 전일 대비율(단위 : %)
        private Double prdyCtrt;
        // 상한가
        private Integer mxpr;
        // 하한가
        private Integer llam;
        // 누적 거래량(단위 : 주)
        private Long acmlVol;
    }
}
