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
@Table(name = "theme_code")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ThemeCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true, columnDefinition = "BIGINT(20) UNSIGNED")
    private Long idx;
    private String groupCode; // 그룹코드(하나증권 테마 그룹 코드)
    @Column(length = 100)
    private String themeName; // 테마명
    private Double prdyCtrt; // 테마 전일대비 등락률(%)
    private Double expectedPrdyCtrt; // 평균예상등락률
    private Double averagePriceRatio; // 평균가대비율
    private LocalDateTime scrapDate; // 등락률 수집일
}
