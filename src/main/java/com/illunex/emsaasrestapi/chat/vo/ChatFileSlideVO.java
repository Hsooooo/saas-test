package com.illunex.emsaasrestapi.chat.vo;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.time.ZonedDateTime;

@Getter
@Setter
@Alias("ChatFileSlideVO")
public class ChatFileSlideVO {
    private Long idx;
    private Long chatFileIdx;
    private String content;
    private Integer page;
    private ZonedDateTime createDate;
    private ZonedDateTime updateDate;
}
