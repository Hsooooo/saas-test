package com.illunex.emsaasrestapi.company.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "company_info")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CompanyInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    // 사업자등록번호
    @Column(length = 13)
    private String bizNum;
    // 법인등록번호
    @Column(length = 13)
    private String corporationNum;
    // 기업명
    private String companyName;
    // 순수 기업명
    private String realCompanyName;
    // 법인 상태
    private String companyState;
    // 대표자명
    @Column(length = 60)
    private String representationName;
    // 기업유형
    private String companyType;
    // 기업 규모
    private String companySize;
    // 직원수
    @Column(length = 10)
    private String employeeCount;
    // 설립일
    @Column(length = 20)
    private String establishmentDate;
    // 재무일자
    @Column(length = 50)
    private String acctMonth;
    // 산업코드 업태(산업코드 앞 1자리 알파벳)
    @Column(length = 1)
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
    @Column(length = 50)
    private String tel;
    // 대표 이메일
    private String email;
    // 팩스
    @Column(length = 20)
    private String fax;
    // 주소
    private String address;
    // 우편번호
    @Column(length = 5)
    private String zipCode;
    // 매출(천원)
    private String sales;
    // 매출년도
    private String salesYear;
    // 대표제품
    private String majorProduct;
    // 본사여부
    @Column(length = 1)
    private String headOffice;
    // 카테고리
    private String category;
    // 키워드
    @Column(length = 500)
    private String keyword;
    // 공개유무(1: 공개, 0: 비공개)
    private boolean visible;
    // 소개
    private String description;
    // 0: 바우처, 1: ked_detail, 2: 대전
    private int originId;
    // 상장시장구분코드(1: 상장, 2: 코스닥, 3: 코넥스, 4: K-OTC, 9: 기타)
    @Column(length = 3)
    private String listingMarketId;
    // 상장코드명
    @Column(length = 10)
    private String listingMarketDesc;
    // 기업국가
    private String country;
    // 로고 URL
    @Column(length = 500)
    private String logoUrl;
    // 일루넥스 번호
    @Column(length = 13)
    private String illuId;
    // 코스콤 수집 성공여부(0: 수집성공, 1: 수집실패, 2: 직접입력)
    private int isKoscomScrapSuccess;
    // 표준종목코드
    @Column(length = 12)
    private String stndIscd;
    // 단축종목코드
    @Column(length = 9)
    private String iscd;
    private LocalDate createDate;
    private LocalDate updateDate;
    private LocalDateTime syncDate;
}
