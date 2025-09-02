package com.illunex.emsaasrestapi.chat.vo;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.time.ZonedDateTime;
import java.util.Map;

@Getter
@Setter
@Alias("ChatLinkVO")
public class ChatLinkVO {
    private Integer idx;
    private Integer chatNetworkIdx;
    private String type;
    private String start;
    private String end;
    private String properties;
    private ZonedDateTime createDate;
    private ZonedDateTime updateDate;
}
