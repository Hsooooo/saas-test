package com.illunex.emsaasrestapi.project.document.project;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "project_filter")
public class ProjectFilter {
    private String categoryName;
    private List<ProjectItem> projectItemList;
}
