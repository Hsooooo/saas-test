package com.illunex.emsaasrestapi.database.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class ResponseDatabaseDTO {

    @Getter
    @Setter
    public static class Search {
        private List<?> results;
        private Integer page;
        private Integer size;
        private Integer totalCount;
    }
}
