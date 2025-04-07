package com.illunex.emsaasrestapi.company.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "company_logos")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CompanyLogos {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;
    /* 주가 분석 */
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
