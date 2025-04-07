package com.illunex.emsaasrestapi.ai.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Comment;

import java.time.LocalDate;

/**
 * 종목과 지수간의 상관 계수 정보
 */
@Getter
@Setter
@Entity
@Table(name = "ai_stock_index_correlation")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiStockIndexCorrelation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Comment("단축종목코드")
    private String stockCode;
    @Comment("거래날짜")
    private LocalDate stockDate;
    @Comment("계산범위(단위 : 월")
    private Integer calcRange;
    @Comment("한국 종합주가지수(kospi, kosdaq)")
    private String koIndex;
    @Comment("한국 종합주가지수 상관계수")
    private Double koIndexCorrelation;
    @Comment("한국 종합주가지수 lag(단위 : 일)")
    private Integer koIndexLag;
    @Comment("미국 종합주가지수(djia, nasdaq)")
    private String usIndex;
    @Comment("미국 종합주가지수 상관 계수")
    private Double usIndexCorrelation;
    @Comment("미국 종합주가지수 lag(단위 : 일)")
    private Integer usIndexLag;
    @Comment("원달러 환율 상관 계수")
    private Double usdKrwCorrelation;
    @Comment("원달러 환율 lag(단위 : 일)")
    private Integer usdKrwLag;
    @Comment("WTI(국제유가) 상관계수")
    private Double wtiCorrelation;
    @Comment("WTI(국제유가) lag(단위 : 일)")
    private Integer wtiLag;
}
