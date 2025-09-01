package com.illunex.emsaasrestapi.chat.vo;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.time.ZonedDateTime;

@Getter
@Setter
@Alias("ChatNetworkVO")
public class ChatNetworkVO {
    private Integer idx;
    private Integer chatHistoryIdx;
    private String title;
    private ZonedDateTime createDate;
    private ZonedDateTime updateDate;
}
