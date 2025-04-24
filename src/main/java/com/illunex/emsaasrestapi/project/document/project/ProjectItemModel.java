package com.illunex.emsaasrestapi.project.document.project;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "project_item_model")
public class ProjectItemModel {
    private String label;
    private String color;
    private Integer start;
    private Integer end;
}
