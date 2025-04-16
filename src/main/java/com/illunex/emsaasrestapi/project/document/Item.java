package com.illunex.emsaasrestapi.project.document;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "item")
public class Item {
    private String label;
    private String nodeType;
    private String fieldName;
    private String fieldType;
    private List<Model> modelList;
}
