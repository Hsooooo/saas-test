package com.illunex.emsaasrestapi.member.vo;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.time.ZonedDateTime;

@Getter
@Setter
@Alias("MemberVO")
public class MemberVO {
    //회원번호
    private Integer idx;
    //이메일주소
    private String email;
    //회원비밀번호
    private String password;
    //프로필이미지URL
    private String profileImageUrl;
    //회원 구분
    private String typeCd;
    //회원 상태
    private String stateCd;
    //간략소개
    private String comment;
    //마지막로그인일자
    private ZonedDateTime lastLoginDate;
    //탈퇴일
    private ZonedDateTime leaveDate;
    //수정일
    private ZonedDateTime updateDate;
    //가입일
    private ZonedDateTime createDate;
}
