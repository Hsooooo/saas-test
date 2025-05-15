package com.illunex.emsaasrestapi.autoComplete.dto;

import lombok.*;
import org.apache.ibatis.ognl.NodeType;

import java.util.List;

public class RequestAutoCompleteDTO {

    @Getter
    public static class AutoCompleteSearch {
        private Integer projectIdx;        // 프로젝트 ID
        private List<String> nodeType;     // 검색할 노트타입
        private String searchKeyword;      // 키워드
        private Integer limit;             // 제한수
    }
}
