package com.illunex.emsaasrestapi.hts.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

/**
 * 산업분류코드-기업 정보 연계 정보
 */
@Getter
@Setter
@Entity
@Table(name = "ksic_company")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KsicCompany {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;
    @Comment("기업번호")
    private Long companyId;
    @Comment("종목별 전일대비 등락률(%)")
    private Double prdyCtrt;
    @Comment("수집 날짜")
    private LocalDateTime scrapDate;
    @Comment("단축종목코드")
    private String iscd;
    @Comment("종목명")
    private String korIsnm;
    @Comment("기업 산업분류코드")
    private String companyBusinessCategoryCode;
    @Comment("산업분류코드 번호")
    private Long ksicIdx;
    @Comment("산업분류코드")
    private String ksicCode;
    @Comment("산업분류코드명")
    private String ksicDesc;
}
