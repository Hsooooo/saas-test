package com.illunex.emsaasrestapi.member.vo;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.time.ZonedDateTime;

@Getter
@Setter
@Alias("MemberLoginHistoryVO")
public class MemberLoginHistoryVO {
    private Integer idx;
    private Integer memberIdx;
    private String browser;
    private String platform;
    private String ip;
    private ZonedDateTime create_date;
}
