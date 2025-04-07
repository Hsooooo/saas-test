package com.illunex.emsaasrestapi.ai.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Comment;

import java.time.LocalDate;

/**
 * 국제유가 정보
 */
@Getter
@Setter
@Entity
@Table(name = "ai_oil_index")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiOilIndex {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Comment("거래날짜")
    private LocalDate stockDate;
    @Comment("WTI(국제유가) 가격")
    private Double wti;
    @Comment("WTI(국제유가) 상승률")
    private Double wtiInc;
    @Comment("WTI(국제유가) 정규화 값")
    private Double wtiStd;
}
