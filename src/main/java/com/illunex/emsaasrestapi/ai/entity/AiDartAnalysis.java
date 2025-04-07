package com.illunex.emsaasrestapi.ai.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Comment;

import java.time.LocalDate;

/**
 * 공시분석
 */
@Getter
@Setter
@Entity
@Table(name = "ai_dart_analysis")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiDartAnalysis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Comment("법인 이름")
    private String corpName;
    @Comment("dart 고유번호")
    private String corpCode;
    @Comment("단축종목코드")
    private String stockCode;
    @Comment("보고서 이름")
    private String reportNm;
    @Comment("공시번호")
    private String rceptNo;
    @Comment("공시날짜")
    private LocalDate rceptDt;
    @Comment("카테고리 분류")
    private String category;
    @Comment("감성분석")
    private String sentiment;
    @Comment("내용 요약")
    private String summary;
    @Comment("생성일")
    private LocalDate created;
}
