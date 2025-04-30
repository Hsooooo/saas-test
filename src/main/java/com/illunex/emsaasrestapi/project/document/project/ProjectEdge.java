package com.illunex.emsaasrestapi.project.document.project;

import lombok.*;
import org.hibernate.annotations.Comment;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "project_edge")
public class ProjectEdge {
    @Comment("엣지 타입(시트명)")
    private String edgeType;
    @Comment("시작 엣지 셀명")
    private String srcEdgeCellName;
    @Comment("시작 노드 타입")
    private String srcNodeType;
    @Comment("시작 노드 셀명")
    private String srcNodeCellName;
    @Comment("도착 엣지 셀명")
    private String destEdgeCellName;
    @Comment("도착 노드 타입")
    private String destNodeType;
    @Comment("도착 노트 셀명")
    private String destNodeCellName;
    @Comment("라벨 표시 엣지 셀명")
    private String labelEdgeCellName;
    @Comment("라벨 표시 엣지 셀타입")
    private String labelEdgeCellType;
    @Comment("라벨 표시 단위(없으면 빈값)")
    private String unit;
    @Comment("엣지 색상")
    private String color;
    @Comment("엣지 방향 여부(true : 시작->도착, false : 방향 표시 x)")
    private Boolean useDirection;
    @Comment("엣지 가중치(선 두께 표시 여부)")
    private Boolean weight;
}
