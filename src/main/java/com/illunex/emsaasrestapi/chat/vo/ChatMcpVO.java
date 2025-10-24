package com.illunex.emsaasrestapi.chat.vo;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.time.ZonedDateTime;

@Setter
@Getter
@Alias("ChatMcpVO")
public class ChatMcpVO {
    private Long idx;
    private Integer chatHistoryIdx;
    private String name;
    private ZonedDateTime createDate;
    private ZonedDateTime updateDate;
}
