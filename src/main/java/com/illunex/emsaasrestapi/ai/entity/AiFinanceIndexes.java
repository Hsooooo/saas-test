package com.illunex.emsaasrestapi.ai.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 금융지수 정보
 */
@Getter
@Setter
@Entity
@Table(name = "ai_finance_indexes")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiFinanceIndexes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Comment("거래날짜")
    private LocalDate stockDate;
    @Comment("원달러 환율")
    private BigDecimal usdKrw;
    @Comment("원달러 환율 증가율(전일대비)")
    private Double usdKrwInc;
    @Comment("원달러 환율 정규화 값")
    private Double usdKrwStd;
    @Comment("다우존스 지수")
    private BigDecimal dji;
    @Comment("다우존스 지수 증가율(전일대비)")
    private Double djiInc;
    @Comment("다우존수 지수 정규화 값")
    private Double djiStd;
    @Comment("코스닥 지수")
    private BigDecimal kosdaq;
    @Comment("코스닥 지수 증가율(전일대비)")
    private Double kosdaqInc;
    @Comment("코스닥 지수 정규화 값")
    private Double kosdaqStd;
    @Comment("코스피 지수")
    private BigDecimal kospi;
    @Comment("코스피 지수 증가율(전일대비)")
    private Double kospiInc;
    @Comment("코스피 지수 정규화 값")
    private Double kospiStd;
    @Comment("나스닥 지수")
    private BigDecimal nasdaq;
    @Comment("나스닥 지수 증가율(전일대비)")
    private Double nasdaqInc;
    @Comment("나스닥 지수 정규화 값")
    private Double nasdaqStd;
}
