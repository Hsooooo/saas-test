package com.illunex.emsaasrestapi.project.document.project;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "project")
public class Project {
    @Id
    private ProjectId projectId;
    private String title;
    private String description;
    private List<ProjectNode> projectNodeList;
    private List<ProjectEdge> projectEdgeList;
    private List<ProjectNodeSize> projectNodeSizeList;
    private List<ProjectFilter> projectFilterList;
    private List<ProjectAttribute> projectAttributeList;
}
