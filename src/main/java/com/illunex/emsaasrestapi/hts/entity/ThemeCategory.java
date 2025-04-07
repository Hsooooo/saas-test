package com.illunex.emsaasrestapi.hts.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

/**
 * 관계망 테마 카테고리
 */
@Getter
@Setter
@Entity
@Table(name = "theme_category")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ThemeCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;
    @Comment("테마명")
    @Column(length = 20)
    private String theme_name;
    @Comment("테마 목록")
    private String theme_list;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;
}
