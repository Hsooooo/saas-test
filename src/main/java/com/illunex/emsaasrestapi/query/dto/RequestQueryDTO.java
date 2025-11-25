package com.illunex.emsaasrestapi.query.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.illunex.emsaasrestapi.common.code.EnumCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

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
        private String queryCategoryName;
        private List<RequestProjectQuery> queryList;
    }

    @Getter
    public static class RequestProjectQuery {
        private EnumCode.ProjectQuery.TypeCd queryType;  // 쿼리 종류 (Select, Update)
        private String queryTitle;   // 쿼리 이름
        private JsonNode rawQuery;    // raw 쿼리
    }

    @Getter
    public static class ExecuteQuery {
        private Integer projectIdx;
        private String rawQuery;
        private Integer skip = 0;
        private Integer limit = 100;
    }

    @Getter
    public static class AIQuery {
        private Integer projectIdx;
        private String queryPrompt;
    }

    @Getter
    @Setter
    public static class AIQueryRequest {
        private String query;
        private List<Map<String, Object>> excel_info;
    }
}
