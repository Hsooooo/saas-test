package com.illunex.emsaasrestapi.partnership.vo;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.time.ZonedDateTime;

@Getter
@Setter
@Alias("PartnershipMemberVO")
public class PartnershipMemberVO {
    //파트너십 회원번호
    private Integer idx;
    //파트너쉽 번호
    private Integer partnershipIdx;
    //파트너십 팀 번호
    private Integer partnershipTeamIdx;
    //회원번호
    private Integer memberIdx;
    //파트너십 직급 번호
    private Integer partnershipPositionIdx;
    //파트너쉽 관리구분
    private String managerCd;
    //파트너쉽 회원상태
    private String stateCd;
    //프로필이미지URL
    private String profileImageUrl;
    //프로필이미지경로
    private String profileImagePath;
    //사용제한라이센스
    private String disableFunctions;
    private ZonedDateTime updateDate;
    private ZonedDateTime createDate;
}
