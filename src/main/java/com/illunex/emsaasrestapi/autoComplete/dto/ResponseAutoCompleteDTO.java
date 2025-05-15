package com.illunex.emsaasrestapi.autoComplete.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

public class ResponseAutoCompleteDTO {

    @Getter
    @Setter
    @Builder
    public static class AutoComplete {
        private Object nodeId;
        private String nodeLabelTitle;
    }
}
