package com.illunex.emsaasrestapi.chat.dto;

import com.illunex.emsaasrestapi.common.code.EnumCode;
import com.illunex.emsaasrestapi.project.document.network.Edge;
import com.illunex.emsaasrestapi.project.document.network.Node;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class ResponseChatDTO {
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    public static class History {
        private Integer idx;
        private Integer chatRoomIdx;
        private String message;
        private String senderType;
        private String categoryType;
        private String categoryTypeDesc;
        private ZonedDateTime createDate;
        private ZonedDateTime updateDate;
        private List<ToolResult> toolResults;

        public void setCategoryType(String categoryType) {
            this.categoryType = categoryType;
            this.categoryTypeDesc = EnumCode.getCodeDesc(categoryType);
        }
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    public static class ToolResult {
        private Long idx;
        private Integer chatHistoryIdx;
        private String toolType;
        /** 통으로 저장할 JSON (문자열) */
        private String title;
        private String url;
        private ZonedDateTime createDate;
        private ZonedDateTime updateDate;
    }
}
