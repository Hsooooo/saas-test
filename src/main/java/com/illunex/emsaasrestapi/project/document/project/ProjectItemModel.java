package com.illunex.emsaasrestapi.project.document.project;

import lombok.*;
import org.hibernate.annotations.Comment;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "project_item_model")
public class ProjectItemModel {
    @Comment("라벨 표시 범례명")
    private String label;
    @Comment("범례 색상")
    private String color;
    @Comment("범례 시작 범위")
    private Integer start;
    @Comment("범례 끝 범위")
    private Integer end;
    @Comment("범례 선택 값")
    private String value;
}
