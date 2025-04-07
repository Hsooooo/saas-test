package com.illunex.emsaasrestapi.ai.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Comment;

import java.time.LocalDate;

/**
 * 종목 간의 상관관계 정보
 */
@Getter
@Setter
@Entity
@Table(name = "ai_stock_correlation")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiStockCorrelation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Comment("시작 단축종목코드")
    private String sourceStockCode;
    @Comment("끝 단축종목코드")
    private String destStockCode;
    @Comment("거래날짜")
    private LocalDate stockDate;
    @Comment("상관 계수")
    private Double correlation;
    @Comment("계산범위(단위 : 월)")
    private Integer calcRange;
    @Comment("상관관계 lag(단위 : 일)")
    private Integer lag;
    @Comment("대상 종목 종가")
    private Integer destStockClose;
    @Comment("대상 종목 전일 대비 상승률")
    private Double destStockIncRate;
}
