package com.illunex.emsaasrestapi.database.dto;

import lombok.Getter;

public class RequestDatabaseDTO {
    @Getter
    public static class Search {
        private DocType docType;
        private String docName;
    }

    public enum DocType {
        Node,
        Link
    }

}
