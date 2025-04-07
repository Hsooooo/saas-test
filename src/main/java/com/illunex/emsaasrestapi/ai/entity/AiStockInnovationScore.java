package com.illunex.emsaasrestapi.ai.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Comment;

/**
 * 혁신성지수 정보
 */
@Getter
@Setter
@Entity
@Table(name = "ai_stock_innovation_score")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiStockInnovationScore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Comment("단축종목코드")
    private String stockCode;
    @Comment("사업자번호")
    private String businessNumber;
    @Comment("기준년도")
    private Integer baseYear;
    @Comment("RS 점수(혁신 인프라)")
    private Double rsScore;
    @Comment("AC 점수(혁신 활동)")
    private Double acScore;
    @Comment("OC 점수(혁신 산출)")
    private Double ocScore;
    @Comment("OP 점수(혁신 성과)")
    private Double opScore;
    @Comment("x축(혁신성 점수)")
    private Double xAxis;
    @Comment("y축(안정성 점수)")
    private Double yAxis;
    @Comment("전체 순위")
    private Integer rank;
    @Comment("총 점수")
    private Float totalScore;
    @Comment("x,y 축 기반 점수 그룹(1:혁신도약형, 2:안정추구형, 3:혁신성장형, 4:잠재성장형)")
    private Integer totalGroup;
}
