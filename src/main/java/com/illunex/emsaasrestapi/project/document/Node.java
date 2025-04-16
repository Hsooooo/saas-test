package com.illunex.emsaasrestapi.project.document;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "node")
public class Node {
    private String nodeType;
    private String fieldName;
    private String fieldType;
}
