package com.illunex.emsaasrestapi.project.document.project;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "project_id")
public class ProjectId {
    private Integer projectCategoryIdx;
    private Integer projectIdx;
    private Integer partnershipIdx;
}
