package com.illunex.emsaasrestapi.ai.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Comment;

import java.time.LocalDate;

/**
 * 주식가격 정보
 */
@Getter
@Setter
@Entity
@Table(name = "ai_stock_values")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiStockValues {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Comment("단축종목코드")
    private String stockCode;
    @Comment("거래날짜")
    private LocalDate stockDate;
    @Comment("종가")
    private Integer close;
    @Comment("종가 정규화값")
    private Double stdClose;
    @Comment("전일대비 상승률")
    private Double incRate;
}
