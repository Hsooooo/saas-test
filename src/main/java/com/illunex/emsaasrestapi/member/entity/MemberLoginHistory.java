package com.illunex.emsaasrestapi.member.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;

import java.time.ZonedDateTime;

@Getter
@Setter
@Entity
@Table(name = "member_login_history")
@Comment("로그인 이력 정보")
public class MemberLoginHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idx;
    @Comment("회원번호")
    private Integer memberIdx;
    @Comment("접속 브라우저")
    private String browser;
    @Comment("접속환경")
    private String platform;
    @Comment("접속 IP")
    private String ip;
    @Comment("접속일")
    private ZonedDateTime createDate;

    @PrePersist
    public void prePersist() {
        this.createDate = ZonedDateTime.now();
    }
}
