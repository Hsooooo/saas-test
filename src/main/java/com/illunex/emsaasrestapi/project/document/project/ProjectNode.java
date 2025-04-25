package com.illunex.emsaasrestapi.project.document.project;

import lombok.*;
import org.hibernate.annotations.Comment;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "project_node")
public class ProjectNode {
    @Comment("노드 타입(시트명)")
    private String nodeType;
    @Comment("셀명")
    private String cellName;
    @Comment("셀 타입")
    private String cellType;
}
