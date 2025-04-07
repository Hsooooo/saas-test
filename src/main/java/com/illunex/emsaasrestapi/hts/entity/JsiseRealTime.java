package com.illunex.emsaasrestapi.hts.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

/**
 * 실시간 시세 정보
 */
@Getter
@Setter
@Entity
@Table(name = "jsise_real_time")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JsiseRealTime {
    @Id
    private String iscd;
    @Comment("일자 (단위 : YYYYMMDD)")
    private Integer bsopDate;
    @Comment("시간 (단위 : HHMMSS)")
    private Integer bsopHour;
    @Comment("한글 종목명")
    private String korIsnm;
    @Comment("현재가")
    private Long prpr;
    @Comment("전일대비율(단위: %)")
    private Double prdyCtrt;
    @Comment("전일 대비")
    private Long prdyVrss;
    @Comment("전일 대비 부호(1:상한 2:상승 3:보합 4:하한 5:하락)")
    private String prdyVrssSign;
    @Comment("누적 거래량(단위: 주)")
    private Long acmlVol;
    @Comment("누적 거래 대금 (단위 : 원)")
    private Long acmlTrPbmn;
    @Comment("시가")
    private Long oprc;
    @Comment("고가")
    private Long hgpr;
    @Comment("저가")
    private Long lwpr;
    @Comment("상한가")
    private Long mxpr;
    @Comment("하한가")
    private Long llam;
    @Comment("시가총액(단위: 원)")
    private Long avls;
    @Comment("전일거래량 (단위: 주)")
    private Long prdyVol;
    @Comment("거래정지여부")
    private String trhtYn;
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
    private LocalDateTime createDate;
    private LocalDateTime updateDate;
}
