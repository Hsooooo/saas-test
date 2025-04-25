package com.illunex.emsaasrestapi.project.document.project;

import lombok.*;
import org.hibernate.annotations.Comment;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "project_node_content_cell")
public class ProjectNodeContentCell {
    @Comment("라벨 표시명")
    private String label;
    @Comment("라벨 표시 셀명")
    private String cellName;
    @Comment("라벨 표시 셀타입")
    private String cellType;
}
