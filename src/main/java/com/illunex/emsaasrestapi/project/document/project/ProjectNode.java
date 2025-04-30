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
    @Comment("노드 고유키 셀명")
    private String uniqueCellName;
    @Comment("라벨 표시 셀명")
    private String labelCellName;
    @Comment("라벨 표시 셀타입")
    private String labelCellType;
}
