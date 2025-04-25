package com.illunex.emsaasrestapi.project.document.project;

import lombok.*;
import org.hibernate.annotations.Comment;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "project_node_size")
public class ProjectNodeSize {
    @Comment("라벨 표시 카테고리명")
    private String labelCategory;
    @Comment("항목 정보")
    private List<ProjectItem> projectItemList;
}
