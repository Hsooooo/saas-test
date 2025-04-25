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
@Document(collection = "project_item")
public class ProjectItem {
    @Comment("라벨 표시 하위 카테고리명")
    private String label;
    @Comment("노드 타입(시트명)")
    private String nodeType;
    @Comment("노드사이즈 셀명")
    private String cellName;
    @Comment("노드사이즈 셀 타입")
    private String cellType;
    @Comment("범례 정보")
    private List<ProjectItemModel> projectItemModelList;
}
