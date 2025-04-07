package com.illunex.emsaasrestapi.company.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

/**
 * 종목 소개 정보
 */
@Getter
@Setter
@Entity
@Table(name = "company_desc")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CompanyDesc {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Comment("단축종목코드")
    private String iscd;
    @Comment("종목 소개")
    private String descript;
    @Comment("수정일")
    private LocalDateTime updateDate;
    @Comment("생성일")
    private LocalDateTime createDate;
}