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
import java.util.Map;

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
        private List<ChatFileResult> chatFiles;
        private List<ChatNetwork> chatNetworks;

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

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    public static class ChatFileResult {
        private Long idx;
        private Integer chatHistoryIdx;
        private String fileName;
        private String fileUrl;
        private String filePath;
        private Long fileSize;
        private String fileCd;
        private String fileCdDesc;
        private List<String> slides = new ArrayList<>();
        private ZonedDateTime updateDate;
        private ZonedDateTime createDate;

        public void setFileCd(String fileCd) {
            this.fileCd = fileCd;
            this.fileCdDesc = EnumCode.getCodeDesc(fileCd);
        }
    }


    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    public static class ChatNetwork {

        private Integer idx;
        private Integer chatHistoryIdx;
        private String title;
        private List<ChatNode> nodes;
        private List<ChatLink> links;
        private ZonedDateTime createDate;
        private ZonedDateTime updateDate;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    public static class ChatNode {
        private Integer idx;
        private String id;
        private List<String> labels;
        private Map<String, Object> properties;
    }


    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    public static class ChatLink {
        private Integer idx;
        private String type;
        private String start;
        private String end;
        private Map<String, Object> properties;
    }

}
