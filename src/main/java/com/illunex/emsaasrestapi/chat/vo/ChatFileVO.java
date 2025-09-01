package com.illunex.emsaasrestapi.chat.vo;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.time.ZonedDateTime;

@Getter
@Setter
@Alias("ChatFileVO")
public class ChatFileVO {
    private Long idx;
    private Integer chatHistoryIdx;
    private String fileName;
    /** 통으로 저장할 JSON (문자열) */
    private String fileUrl;
    private String filePath;
    private Long fileSize;
    private String fileCd;
    private ZonedDateTime createDate;
    private ZonedDateTime updateDate;
}
