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
@Document(collection = "node")
public class Node {
    @Id
    private NodeId nodeId;
    private Object id;
    private Object label;
    private LinkedHashMap<String, Object> properties;
}
