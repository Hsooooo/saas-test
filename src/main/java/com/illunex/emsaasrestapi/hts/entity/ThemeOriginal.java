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
@Table(name = "theme_original")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ThemeOriginal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true, columnDefinition = "BIGINT(20) UNSIGNED")
    private Long idx;
    private Long themeIdx; // 테마 코드 idx
    @Column(length = 40)
    private String themeName; // 테마명
    @Column(length = 9)
    private String iscd; // 단축종목 코드
    @Column(length = 40)
    private String korIsnm; // 종목명
    @Column(length = 40)
    private String bstpName; // 업종명
    private Double prdyCtrt; // 종목 전일대비 등락률(%)
    private LocalDateTime scrapDate; // 등락률 수집일
    private String companyOverview; // 기업 개요
}
