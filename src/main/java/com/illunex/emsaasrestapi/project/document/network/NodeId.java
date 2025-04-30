package com.illunex.emsaasrestapi.project.document.network;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "node_id")
public class NodeId {
    private Integer projectIdx;
    private Object nodeIdx;
}
