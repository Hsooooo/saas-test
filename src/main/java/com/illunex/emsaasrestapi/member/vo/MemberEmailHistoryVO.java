package com.illunex.emsaasrestapi.member.vo;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.time.ZonedDateTime;

@Getter
@Setter
@Alias("MemberEmailHistoryVO")
public class MemberEmailHistoryVO {
    private Long idx;
    private Integer memberIdx;
    private String certData;
    private boolean isUsed;
    private String emailType;
    private ZonedDateTime expireDate;
    private ZonedDateTime createDate;
}
