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
@Document(collection = "project_node_content")
public class ProjectNodeContent {
    @Comment("노드 타입(시트명)")
    private String nodeType;
    @Comment("라벨 표시 제목 셀명")
    private String labelTitleCellName;
    @Comment("라벨 표시 내용 셀명")
    private String labelContentCellName;
    @Comment("라벨 표시 키워드 셀명 목록")
    private List<String> labelKeywordCellList;
    @Comment("라벨 표시 키워드 구분자")
    private String keywordSplitUnit;
    @Comment("노드 속성 셀 정보")
    private List<ProjectNodeContentCell> projectNodeContentCellList;
}
