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
    @Comment("노드 타입별 카운트")
    private List<ProjectNodeCount> projectNodeCountList;
    @Comment("엣지 타입별 카운트")
    private List<ProjectEdgeCount> projectEdgeCountList;
    @Comment("최대 노드 개수")
    private Integer maxNodeSize;
    @Comment("총 데이터 개수")
    private Integer totalDataCount;
    @Comment("수정일")
    private LocalDateTime updateDate;
    @Comment("등록일")
    private LocalDateTime createDate;
}
