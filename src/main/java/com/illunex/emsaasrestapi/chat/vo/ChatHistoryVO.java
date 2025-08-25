package com.illunex.emsaasrestapi.chat.vo;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.time.ZonedDateTime;

@Getter
@Setter
@Alias("ChatHistoryVO")
public class ChatHistoryVO {
    private Integer idx;
    private Integer chatRoomIdx;
    private String message;
    private String senderType;
    private ZonedDateTime createDate;
    private ZonedDateTime updateDate;
}
