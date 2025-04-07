package com.illunex.emsaasrestapi.member.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;

import java.time.ZonedDateTime;

@Getter
@Setter
@Entity
@Table(name = "member_email_history")
@Comment("회원 메일 전송 이력")
public class MemberEmailHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idx;
    @Comment("회원번호")
    private Integer memberIdx;
    @Comment("인증키")
    private String certData;
    @Comment("사용여부")
    private Boolean used;
    @Comment("메일구분(code테이블)")
    private String emailType;
    @Comment("만료일")
    private ZonedDateTime expireDate;
    @Comment("생성일")
    private ZonedDateTime createDate;

    @PrePersist
    public void prePersist() {
        this.createDate = ZonedDateTime.now();
    }
}
