package com.illunex.emsaasrestapi.chat.vo;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.time.ZonedDateTime;

@Getter
@Setter
@Alias("ChatToolResultVO")
public class ChatToolResultVO {
    private Long idx;
    private Integer chatHistoryIdx;
    private String toolType;
    /** 통으로 저장할 JSON (문자열) */
    private String title;
    private String url;
    private ZonedDateTime createDate;
    private ZonedDateTime updateDate;
}
