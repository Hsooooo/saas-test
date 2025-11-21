package com.illunex.emsaasrestapi.database;

public class MongoDBUtils {

    public enum Node {
        // NodeId
        _ID_PROJECT_IDX("_id.projectIdx"),
        _ID_TYPE("_id.type"),
        _ID_NODE_IDX("_id.nodeIdx"),

        ID("id"),
        LABEL("label"),

        PROPERTIES("properties");

        public String field;

        Node(String field) {
            this.field = field;
        }

        public String getField() {
            return field;
        }

        public String getPropertyField(String propertyName) {
            return PROPERTIES.field + "." + propertyName;
        }
    }

    public enum Edge {
        // EdgeId
        _ID_PROJECT_IDX("_id.projectIdx"),
        _ID_TYPE("_id.type"),
        _ID_EDGE_IDX("_id.edgeIdx"),

        ID("id"),
        START_TYPE("startType"),
        START("start"),
        END_TYPE("endType"),
        END("end"),
        TYPE("type"),

        PROPERTIES("properties");

        public String field;

        Edge(String field) {
            this.field = field;
        }

        public String getField() {
            return field;
        }

        public String getPropertyField(String propertyName) {
            return PROPERTIES.field + "." + propertyName;
        }
    }

    public enum Column {
        ID("_id"),

        PROJECT_IDX("projectIdx"),
        TYPE("type"),

        COLUMN_DETAIL_LIST("columnDetailList");

        public String field;

        Column(String field) {
            this.field = field;
        }

        public String getField() {
            return field;
        }

        public String getColumnDetailField(String columnDetailName) {
            return COLUMN_DETAIL_LIST.field + "." + columnDetailName;
        }
    }
}
