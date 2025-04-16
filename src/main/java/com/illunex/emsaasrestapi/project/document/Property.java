package com.illunex.emsaasrestapi.project.document;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "property")
public class Property {
    private String NodeType;
    private String labelTitleFieldName;
    private String labelContentFieldName;
    private List<String> labelKeywordList;
    private String keywordSplitValue;
    private List<Field> fieldList;
}
