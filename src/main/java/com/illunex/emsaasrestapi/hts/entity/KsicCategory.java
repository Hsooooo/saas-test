package com.illunex.emsaasrestapi.hts.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

/**
 * 한국산업분류코드 11차 중분류
 */
@Getter
@Setter
@Entity
@Table(name = "ksic_category")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KsicCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;
    @Comment("산업분류코드")
    private String code;
    @Comment("산업분류코드명")
    private String codeDesc;
    @Comment("산업분류코드 전일대비 등락률(%)")
    private Double prdyCtrt;
    private LocalDateTime scrapDate;
    private LocalDateTime updateDate;
    private LocalDateTime createDate;
}
