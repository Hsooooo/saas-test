package com.illunex.emsaasrestapi.chat.vo;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.time.ZonedDateTime;

@Getter
@Setter
@Alias("ChatRoomVO")
public class ChatRoomVO {
    private Integer idx;
    private Integer partnershipMemberIdx;
    private String title;
    private ZonedDateTime deleteDate;
    private ZonedDateTime createDate;
    private ZonedDateTime updateDate;
}
