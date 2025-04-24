package com.illunex.emsaasrestapi.project.document.project;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "project_edge")
public class ProjectEdge {
    private String edgeType;
    private String srcNodeType;
    private String srcFieldName;
    private String destNodeType;
    private String destFieldName;
    private String labelFieldName;
    private String labelFieldType;
    private String unit;
    private String color;
    private Boolean useDirection;
    private Boolean weight;
}
