package com.illunex.emsaasrestapi.hts.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "jstock_jong")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JStockJong {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true, columnDefinition = "BIGINT(20) UNSIGNED")
    private Long idx;

    @Column(length = 12)
    @Comment("표준종목코드")
    private String stndIscd;

    @Column(length = 9)
    @Comment("단축 종목코드")
    private String iscd;

    @Column(length = 40)
    @Comment("한글 종목명")
    private String korIsnm;

    @Column(length = 1)
    @Comment("시장 분류 구분 코드(1:코스피,2:코스닥,A:코스피(파생),P:코넥스)")
    private String mrktDivClsCode;

    @Column(length = 3)
    @Comment("증권그룹구분코드(ST:토큰증권, RT:부동산펀드, IF:인프라펀드, MF:뮤추얼펀드, FS:외국증권, DR:예탁증서상장증권, EF:ETF, EN:ETN)")
    private String scrtGrpClsCode;

    @Column(length = 1)
    @Comment("시가총액 규모 구분코드")
    private String avlsScalClsCode;

    @Column(length = 4)
    @Comment("업종대분류코드")
    private String bstpLargDivCode;

    @Column(length = 4)
    @Comment("업종중분류코드")
    private String bstpMedmDivCode;

    @Column(length = 4)
    @Comment("업종소분류코드")
    private String bstpSmalDivCode;

    @Column(length = 2)
    @Comment("재조업구분코드")
    private String mninClsCode;

    @Column(length = 2)
    @Comment("배당지수종목여부")
    private String dvdnNmixIssuYn;

    @Column(length = 2)
    @Comment("지배구조우량여부")
    private String sprnStrrSprrYn;

    @Column(length = 1)
    @Comment("KOSPI200채용구분코드")
    private String kospi200_apntClsCode;

    @Column(length = 2)
    @Comment("KOSPI100종목여부")
    private String kospi100_issuYn;

    @Column(length = 2)
    @Comment("지배구조지수종목여부")
    private String sprnStrrNmixIssuYn;

    @Column(length = 2)
    @Comment("KRX100종목여부")
    private String krx100_issuYn;

    @Column(length = 2)
    @Comment("결산월")
    private String stacMonth;

    @Comment("액면가")
    private Float stckFcam;

    @Comment("주식 기준가")
    private Integer stckSdpr;

    @Comment("자본금")
    private Double lstnCpf;

    @Comment("상장주수")
    private Long lstnStcn;

    @Comment("배당수익율")
    private Double dvdnErt;

    @Comment("신용잔고비율")
    private Float crdtRmndRate;

    @Column(length = 1)
    @Comment("거래정지여부(Y:정상, N:거래정지)")
    private String trhtYn;

    @Column(length = 1)
    @Comment("정리매매여부(Y:정상, N:정리매매)")
    private String sltrYn;

    @Column(length = 1)
    @Comment("관리 종목 여부")
    private String mangIssuYn;

    @Column(length = 2)
    @Comment("시장 경고 구분 코드")
    private String mrktAlrmClsCode;

    @Column(length = 1)
    @Comment("시장 경고 예고")
    private String mrktAlrmRiskAdntYn;

    @Column(length = 1)
    @Comment("불성실공시여부")
    private String insnPbntYn;

    @Column(length = 1)
    @Comment("우회상장여부")
    private String bypsLstnYn;

    @Column(length = 2)
    @Comment("락구분코드")
    private String flngClsCode;

    @Comment("당사신용비율")
    private Float crdtRate;

    @Column(length = 1)
    @Comment("고위험종목 여부")
    private String highRisk;

    @Column(length = 1)
    @Comment("Equity Rating A,B,C,D,F")
    private String equRating;

    @Comment("시가총액 (단위 : 원)")
    private Long avls;

    @Comment("외국인보유비율")
    private Double frgnHldnRate;

    @Column(length = 1)
    @Comment("우선주구분코드(1:우선주, 0:해당없음)")
    private String prstClsCode;

    @Comment("등록일시")
    protected LocalDateTime createDate;

    @Comment("수정일시")
    protected LocalDateTime updateDate;
}
