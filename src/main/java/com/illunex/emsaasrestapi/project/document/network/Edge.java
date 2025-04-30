package com.illunex.emsaasrestapi.project.document.network;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.LinkedHashMap;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "edge")
public class Edge {
    @Id
    private EdgeId edgeId;
    private Object id;
    private Object startType;
    private Object start;
    private Object endType;
    private Object end;
    private String type;
    private LinkedHashMap<String, Object> properties;
}
