package com.illunex.emsaasrestapi.project.document.data;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "data")
public class Data {
    @Id
    private Integer projectIdx;
    private List<DataSheet> dataSheet;
}
