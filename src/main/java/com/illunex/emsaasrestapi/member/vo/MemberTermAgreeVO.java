package com.illunex.emsaasrestapi.member.vo;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.time.ZonedDateTime;

@Getter
@Setter
@Alias("MemberTermAgreeVO")
public class MemberTermAgreeVO {
    //약관 동의 정보 번호
    private Integer idx;
    //회원번호
    private Integer memberIdx;
    //약관번호
    private Integer memberTermIdx;
    //동의여부
    private Boolean agree;
    //수정일
    private ZonedDateTime updateDate;
    //등록일
    private ZonedDateTime createDate;
}
