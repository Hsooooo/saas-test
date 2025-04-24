package com.illunex.emsaasrestapi.project.document.project;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "project_attribute_field")
public class ProjectAttributeField {
    private String label;
    private String fieldName;
    private String fieldType;
}
