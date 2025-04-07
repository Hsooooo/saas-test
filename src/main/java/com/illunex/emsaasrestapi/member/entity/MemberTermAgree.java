package com.illunex.emsaasrestapi.member.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;

import java.time.ZonedDateTime;

@Getter
@Setter
@Entity
@Builder
@Table(name = "member_term_agree")
@Comment("약관 동의 정보")
public class MemberTermAgree {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idx;
//    @Comment("회원번호")
//    private Integer memberIdx;
//    @Comment("약관번호")
//    private String termIdx;
    @Comment("동의 여부(1:동의, 0:미동의)")
    private Boolean agree;
    @Comment("수정일")
    private ZonedDateTime updateDate;
    @Comment("등록일")
    private ZonedDateTime createDate;

    @ManyToOne
    @JoinColumn(referencedColumnName = "idx")
    private Member member;
    @ManyToOne
    @JoinColumn(referencedColumnName = "idx")
    private MemberTerm memberTerm;

    @PrePersist
    public void prePersist() {
        this.createDate = ZonedDateTime.now();
    }
}
