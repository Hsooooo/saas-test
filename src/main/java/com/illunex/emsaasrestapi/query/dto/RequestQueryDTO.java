package com.illunex.emsaasrestapi.query.dto;

import lombok.Getter;

public class RequestQueryDTO {
    @Getter
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

    @Getter
    public static class SaveQuery {
        private Integer projectIdx;
        private QueryCategory queryCategory;
        private String queryTitle;
        private String rawQuery;
    }

    @Getter
    public static class QueryCategory {
        private Integer queryCategoryIdx;
        private String categoryName;
    }
}
