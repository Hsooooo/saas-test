package com.illunex.emsaasrestapi.autoComplete.dto;

import lombok.Getter;
import lombok.Setter;

public class ResponseAutoCompleteDTO {

    @Getter
    @Setter
    public static class AutoComplete {
        private Long nodeIdx;
        private String keyword;
    }
}
