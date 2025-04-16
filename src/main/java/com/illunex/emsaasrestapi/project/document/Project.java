package com.illunex.emsaasrestapi.project.document;

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
    private List<Node> nodeList;
    private List<Edge> edgeList;
    private List<NodeSize> nodeSizeList;
}
