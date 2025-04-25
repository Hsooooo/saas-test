package com.illunex.emsaasrestapi.project.document.project;

import lombok.*;
import org.hibernate.annotations.Comment;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "project_id")
public class ProjectId {
    @Comment("프로젝트 카테고리번호")
    private Integer projectCategoryIdx;
    @Comment("프로젝트번호")
    private Integer projectIdx;
    @Comment("파트너쉽번호")
    private Integer partnershipIdx;
}
