package com.illunex.emsaasrestapi.project.document;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "projectId")
public class ProjectId {
    private Integer projectIdx;
    private Integer partnershipIdx;
}
