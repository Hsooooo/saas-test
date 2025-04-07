package com.illunex.emsaasrestapi.member.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;

import java.time.ZonedDateTime;

@Getter
@Setter
@Entity
@Table(name = "member")
@Comment("회원 정보")
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idx;
    @Comment("이메일")
    private String email;
    @Comment("회원 비밀번호")
    private String password;
    @Comment("회원 닉네임(최대 128자)")
    private String nickname;
    @Comment("프로필 이미지 URL")
    private String profileImageUrl;
    @Comment("프로필 이미지 path")
    private String profileImagePath;
    @Comment("회원 구분(code 테이블)")
    private String typeCd;
    @Comment("회원 상태(code 테이블)")
    private String stateCd;
    @Comment("간략 소개(최대 100자)")
    private String comment;
    @Comment("탈퇴일")
    private ZonedDateTime leaveDate;
    @Comment("수정일")
    private ZonedDateTime updateDate;
    @Comment("가입일")
    private ZonedDateTime createDate;

    @PrePersist
    public void prePersist() {
        this.createDate = ZonedDateTime.now();
    }
}
