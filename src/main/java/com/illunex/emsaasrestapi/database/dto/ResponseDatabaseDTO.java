package com.illunex.emsaasrestapi.database.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
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

    @Getter
    @Setter
    public static class EdgeResult {
        private Object id;
        private LinkedHashMap<String, Object> properties;
    }

    @Getter
    @Setter
    public static class DatabaseList {
        private List<NodeStat> nodeStatList = new ArrayList<>();
        private List<EdgeStat> edgeStatList = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class NodeStat {
        private String type;
        private String uniqueCellName;
        private String labelCellName;
        private Long count;
    }

    @Getter
    @Setter
    public static class EdgeStat {
        private String type;
        private String unit;
        private String color;
        private Boolean isDirection;
        private Boolean isWeight;
        private Long count;
    }

}
