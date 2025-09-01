package com.illunex.emsaasrestapi.chat.vo;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.time.ZonedDateTime;
import java.util.Map;

@Setter
@Getter
@Alias("ChatNodeVO")
public class ChatNodeVO {
    private Integer idx;
    private Integer chatNetworkIdx;
    private String id;
    private String labels;
    private String properties;
    private ZonedDateTime createDate;
    private ZonedDateTime updateDate;
}
