package com.illunex.emsaasrestapi.database.dto;

import com.illunex.emsaasrestapi.project.dto.ResponseProjectDTO;
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
        private ResponseProjectDTO.Project project;
        private String projectCategoryName;
        private List<TableData> nodeTableList = new ArrayList<>();
        private List<TableData> edgeTableList = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class TableData {
        private String title;
        private Long dataCount;
        private String typeCd;
        private String typeCdDesc;
        private List<TableAuthMember> members = new ArrayList<>();
    }

    public record TableAuthMember (
        Integer idx,
        String name,
        String email,
        String profileImageUrl
    ) {}

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

    @Getter
    @Setter
    public static class Commit {
        private Integer createdCount;
        private Integer updatedCount;
        private Integer deletedCount;
    }

}
