package com.illunex.emsaasrestapi.partnership.vo;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.time.ZonedDateTime;

@Getter
@Setter
@Alias("PartnershipMemberPreviewVO")
public class PartnershipMemberPreviewVO {
    //파트너쉽 회원 idx
    private Integer idx;
    //회원이름
    private String name;
    //프로필이미지URL
    private String profileImageUrl;
    //프로필이미지경로
    private String profileImagePath;

}
