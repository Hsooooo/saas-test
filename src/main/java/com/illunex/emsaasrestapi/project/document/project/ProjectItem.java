package com.illunex.emsaasrestapi.project.document.project;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "project_item")
public class ProjectItem {
    private String label;
    private String nodeType;
    private String fieldName;
    private String fieldType;
    private List<ProjectItemModel> projectItemModelList;
}
