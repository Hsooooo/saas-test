package com.illunex.emsaasrestapi.member.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;

import java.time.ZonedDateTime;

@Getter
@Setter
@Entity
@Table(name = "member_term")
@Comment("약관 정보")
public class MemberTerm {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idx;
    @Comment("약관 제목")
    private String subject;
    @Comment("약관 내용")
    private String content;
    @Comment("활성화 여부(1:활성화, 0:비활성화)")
    private Boolean active;
    @Comment("필수 여부(1:필수, 0:선택)")
    private Boolean required;
    @Comment("수정일")
    private ZonedDateTime updateDate;
    @Comment("등록일")
    private ZonedDateTime createDate;

    @PrePersist
    public void prePersist() {
        this.createDate = ZonedDateTime.now();
    }
}
