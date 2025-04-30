package com.illunex.emsaasrestapi.project.document.network;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "edge_id")
public class EdgeId {
    private Integer projectIdx;
    private Integer edgeIdx;
}
