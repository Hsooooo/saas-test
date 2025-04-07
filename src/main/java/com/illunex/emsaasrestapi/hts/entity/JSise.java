package com.illunex.emsaasrestapi.hts.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "jsise")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JSise {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true, columnDefinition = "BIGINT(20) UNSIGNED")
    private Long idx;
    // 단축 종목코드
    private String iscd;
    // 표준종목코드
    @Column(name = "stnd_iscd")
    private String stndIscd;
    // 현재가
    private Integer prpr;
    // 한글 종목명
    @Column(name = "kor_isnm")
    private String korIsnm;
    // 전일 대비
    @Column(name = "prdy_vrss")
    private Integer prdyVrss;
    // 전일 대비 부호(1:상한, 2:상승, 3:보합, 4:하한, 5:하락
    private String prdyVrssSign;
    // 전일 대비율(단위 : %)
    @Column(name = "prdy_ctrt")
    private Double prdyCtrt;
    // 시간(단위 : HHMMSS)
    private Integer bsopHour;
    // 일자(단위 : YYYYMMDD)
    private Integer bsopDate;
    // 전일 종가(단위 : 원)
    private Long prdyClpr;
    // 누적 거래량(단위 : 주)
    private Long acmlVol;
    // 누적 거래 대금(단위 : 원)
    private Long acmlTrPbmn;
    // 시가
    private Integer oprc;
    // 고가
    private Integer hgpr;
    // 저가
    private Integer lwpr;
    // 상한가
    private Integer mxpr;
    // 하한가
    private Integer llam;
    // 시가총액(단위 : 원)
    private Long avls;
    // 전일거래량(단위 : 주)
    private Long prdyVol;
    // 매도호가1
    private Integer askp1;
    // 매도호가2
    private Integer askp2;
    // 매도호가3
    private Integer askp3;
    // 매도호가4
    private Integer askp4;
    // 매도호가5
    private Integer askp5;
    // 매도호가6
    private Integer askp6;
    // 매도호가7
    private Integer askp7;
    // 매도호가8
    private Integer askp8;
    // 매도호가9
    private Integer askp9;
    // 매도호가10
    private Integer askp10;
    // 매수호가1
    private Integer bidp1;
    // 매수호가2
    private Integer bidp2;
    // 매수호가3
    private Integer bidp3;
    // 매수호가4
    private Integer bidp4;
    // 매수호가5
    private Integer bidp5;
    // 매수호가6
    private Integer bidp6;
    // 매수호가7
    private Integer bidp7;
    // 매수호가8
    private Integer bidp8;
    // 매수호가9
    private Integer bidp9;
    // 매수호가10
    private Integer bidp10;
    // 매도호가 잔량1
    private Long askpRsqn1;
    // 매도호가 잔량2
    private Long askpRsqn2;
    // 매도호가 잔량3
    private Long askpRsqn3;
    // 매도호가 잔량4
    private Long askpRsqn4;
    // 매도호가 잔량5
    private Long askpRsqn5;
    // 매도호가 잔량6
    private Long askpRsqn6;
    // 매도호가 잔량7
    private Long askpRsqn7;
    // 매도호가 잔량8
    private Long askpRsqn8;
    // 매도호가 잔량9
    private Long askpRsqn9;
    // 매도호가 잔량10
    private Long askpRsqn10;
    // 매수호가 잔량1
    private Long bidpRsqn1;
    // 매수호가 잔량2
    private Long bidpRsqn2;
    // 매수호가 잔량3
    private Long bidpRsqn3;
    // 매수호가 잔량4
    private Long bidpRsqn4;
    // 매수호가 잔량5
    private Long bidpRsqn5;
    // 매수호가 잔량6
    private Long bidpRsqn6;
    // 매수호가 잔량7
    private Long bidpRsqn7;
    // 매수호가 잔량8
    private Long bidpRsqn8;
    // 매수호가 잔량9
    private Long bidpRsqn9;
    // 매수호가 잔량10
    private Long bidpRsqn10;
    // 52주 최고가
    @Column(name = "w52_hgpr")
    private Integer w52Hgpr;
    // 52주 최고가 일자
    @Column(name = "w52_hgpr_date")
    private Integer w52HgprDate;
    // 52주 최저가
    @Column(name = "w52_lwpr")
    private Integer w52Lwpr;
    // 52주 최저가 일자
    @Column(name = "w52_lwpr_date")
    private Integer w52LwprDate;
    // 액면가
    private Double fcam;
    // 대용가
    private Integer sspr;
    // 기준가
    private Integer sdpr;
    // 상장 주수(단위 : 주)
    private Long lstnStcn;
    // 신 장운영 구분 코드(첫번째비트 - 0:예상, 1:장개시전, 2:장중, 3:장종료후, 4:시간외단일가 / 두번째비트 - 0:보통, 1:종가, 2:대량, 3:바스켓, 7:정리매매)
    private String newMkopClsCode;
    // 외국인보유비율
    private Double frgnHldnRate;
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
    @Column(name = "kospi200_apnt_cls_code")
    private String kospi200ApntClsCode;
    // KRX100 종목여부
    @Column(name = "krx100_issu_yn")
    private String krx100IssuYn;
    // KOSPI 종목여부
    private String kospiIssuYn;
    // KOSPI100 종목여부
    @Column(name = "kospi100_issu_yn")
    private String kospi100IssuYn;
    // KOSPI50 종목여부
    @Column(name = "kospi50_issu_yn")
    private String kospi50IssuYn;
    // 전일종가구분코드
    private String prdyClsCode;
    // 평가가격
    private Integer htsStckVltnPrc;
    // 발행가격
    private Integer stckPblcPrc;
    // 배당수익률
    private Double dvdnErt;
    // 행사가
    private Double acpr;
    // 자본금
    private Double cpfn;
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
    private Integer stckLwstAsprPrc;
    // 주식 최고 호가 가격
    private Integer stckHghsAsprPrc;
    // 정규시장매매수량단위
    private Integer frmlMrktDealQtyUnit;
    // 시간외시장매매수량단위
    private Integer ovtmMrktDealQtyUnit;
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
    @Column(name = "regl_s_yn")
    private String reglSYn;
    // 기업인수목적회사여부
    private String etprUndtObjtCoYn;
    // ETF 유통주수
    private Integer etfCrclStcn;
    // 전일과표기기준고가격
    private Double prdyStasStndPrc;
    // 전일배당전과표기준가격
    private Double prdyDvdnBefStasStndPrc;
    // 전일현금배당금액
    private Double prdyCashDvdnAmt;
    // 전전일과표기준가격
    @Column(name = "d2_bef_stas_stnd_proc")
    private Double d2BefStasStndProc;
    // 대용가사정비율
    private Double ssprAssmRate;
    // 투자주의환기종목여부
    private String warnYn;
    // 과세유형
    private String txtnTypeCode;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;
}
