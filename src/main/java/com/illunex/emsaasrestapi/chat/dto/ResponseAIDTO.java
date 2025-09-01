package com.illunex.emsaasrestapi.chat.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;
import java.util.Map;

public class ResponseAIDTO {
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Graph {
        private int status;
        @JsonProperty("graph_data")
        private GraphData graphData;

        @Data
        @NoArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class GraphData {
            private List<GraphNode> nodes;
            @JsonProperty("relationships")
            private List<GraphRelationship> relationships;
        }

        @Data
        @NoArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class GraphNode {
            private String id;
            private String labels;
            private Map<String, Object> properties;
        }

        @Data
        @NoArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class GraphRelationship {
            private String type;
            private String start;
            private String end;
            private Map<String, Object> properties;
        }
    }
}
