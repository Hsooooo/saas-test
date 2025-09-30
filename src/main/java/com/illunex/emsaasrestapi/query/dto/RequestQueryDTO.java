package com.illunex.emsaasrestapi.query.dto;

import lombok.Getter;

public class RequestQueryDTO {
    @Getter
    public static class FindQuery {
        private Integer projectIdx;
        private String filter;
        private String projection;
        private String sort;
        private Integer skip = 0;
        private Integer limit = 100;
    }

    @Getter
    public static class SaveQuery {
        private Integer projectIdx;
        private Integer partnershipIdx;
        private QueryCategory queryCategory;
        private String queryTitle;
        private String rawQuery;
    }

    @Getter
    public static class QueryCategory {
        private Integer queryCategoryIdx;
        private String categoryName;
    }

    @Getter
    public static class ExecuteQuery {
        private Integer projectIdx;
        private String rawQuery;
        private Integer skip = 0;
        private Integer limit = 100;
    }
}
