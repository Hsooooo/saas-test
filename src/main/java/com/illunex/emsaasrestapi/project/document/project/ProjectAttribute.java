package com.illunex.emsaasrestapi.project.document.project;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "project_attribute")
public class ProjectAttribute {
    private String nodeType;
    private String labelTitleFieldName;
    private String labelContentFieldName;
    private List<String> labelKeywordList;
    private String keywordSplitValue;
    private List<ProjectAttributeField> projectAttributeFieldList;
}
