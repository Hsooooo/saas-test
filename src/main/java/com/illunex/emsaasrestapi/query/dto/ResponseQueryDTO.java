package com.illunex.emsaasrestapi.query.dto;

import com.illunex.emsaasrestapi.common.code.EnumCode;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

public class ResponseQueryDTO {

    @Getter
    @Setter
    @Builder
    public static class ExecuteFind {
        private Long total;
        private Integer page;
        private Integer size;
        private Integer skip;
        private Integer limit;
        private List<Map> result;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Categories {
        private Integer queryCategoryIdx;
        private String categoryName;
    }

    @Setter
    @Getter
    public static class Query {
        private Integer idx;
        private String title;
        private String rawQuery;
        private String typeCd;
        private String typeCdDesc;
        private ZonedDateTime updateDate;
        private ZonedDateTime createDate;

        public void setTypeCd(String typeCd) {
            this.typeCd = typeCd;
            this.typeCdDesc = EnumCode.getCodeDesc(typeCd);
        }
    }

    @Setter
    @Getter
    public static class QueriesByCategory {
        private Integer queryCategoryIdx;
        private String categoryName;
        private List<Query> queries;

        public QueriesByCategory(Integer queryCategoryIdx, String categoryName, List<Query> queries) {
            this.queryCategoryIdx = queryCategoryIdx;
            this.categoryName = categoryName;
            this.queries = queries;
        }
    }
}
