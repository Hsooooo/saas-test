package com.illunex.emsaasrestapi.company.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "company_theme")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CompanyTheme {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;
    // 테마코드 idx
    private Long themeIdx;
    // 단축종목코드
    @Column(length = 9)
    private String iscd;
    // 기업개요
    private String companyOverview;
    private LocalDate createDate;
}
