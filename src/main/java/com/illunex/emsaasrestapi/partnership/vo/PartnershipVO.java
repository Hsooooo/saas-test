package com.illunex.emsaasrestapi.partnership.vo;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.time.ZonedDateTime;

@Getter
@Setter
@Alias("PartnershipVO")
public class PartnershipVO {
    //파트너쉽 번호
    private Integer idx;
    //파트너쉽명
    private String name;
    //파트너쉽도메인
    private String domain;
    //파트너쉽이미지URL
    private String imageUrl;
    //파트너쉽이미지위치
    private String imagePath;
    //파트너쉽간략소개
    private String comment;
    //수정일
    private ZonedDateTime updateDate;
    //생성일
    private ZonedDateTime createDate;
}
