package com.illunex.emsaasrestapi.member.vo;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.time.ZonedDateTime;

@Getter
@Setter
@Alias("MemberTermVO")
public class MemberTermVO {
    //약관번호
    private Integer idx;
    //약관제목
    private String subject;
    //약관내용
    private String content;
    //활성화여부
    private Boolean active;
    //필수여부
    private Boolean required;
    //수정일
    private ZonedDateTime updateDate;
    //등록일
    private ZonedDateTime createDate;
}
