package com.illunex.emsaasrestapi.query.dto;

import lombok.Getter;
import lombok.Setter;

public class ResponseQueryDTO {

    @Getter
    @Setter
    public static class ExecuteQuery {
        private Integer projectIdx;
        private String collection;
        private String operationCd;
        private String filter;
        private String projection;
        private String sort;
        private Integer page;
        private Integer size;
        private Integer skip;
        private Integer limit;
    }
}
