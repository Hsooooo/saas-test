package com.illunex.emsaasrestapi.project.document.project;

import lombok.*;
import org.hibernate.annotations.Comment;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "project")
public class Project {
    @Id
    private Integer projectIdx;
    @Comment("노드 정보")
    private List<ProjectNode> projectNodeList;
    @Comment("엣지 정보")
    private List<ProjectEdge> projectEdgeList;
    @Comment("노드사이즈 정보")
    private List<ProjectNodeSize> projectNodeSizeList;
    @Comment("필터링 정보")
    private List<ProjectFilter> projectFilterList;
    @Comment("노드 속성 정보")
    private List<ProjectNodeContent> projectNodeContentList;
    @Comment("수정일")
    private LocalDateTime updateDate;
    @Comment("등록일")
    private LocalDateTime createDate;
}
