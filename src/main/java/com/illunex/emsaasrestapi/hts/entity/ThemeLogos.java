package com.illunex.emsaasrestapi.hts.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

/**
 * 테마 로고
 */
@Getter
@Setter
@Entity
@Table(name = "theme_logos")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ThemeLogos {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;
    @Comment("테마 idx")
    private Long themeIdx;
    @Comment("테마 로고 URL")
    @Column(length = 500)
    private String fileUrl;
    @Comment("로고 위치")
    @Column(length = 500)
    private String filePath;
    @Comment("파일 원본이름")
    @Column(length = 500)
    private String fileName;
    @Comment("파일 별칭(테마idx_테마명)")
    @Column(length = 500)
    private String fileAlias;
    @Comment("파일크기")
    private Integer fileSize;
    private LocalDateTime update_date; // 수정일
    private LocalDateTime createDate; // 등록일
}
