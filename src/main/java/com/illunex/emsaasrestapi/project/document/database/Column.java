package com.illunex.emsaasrestapi.project.document.database;

import jakarta.persistence.Id;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "column")
public class Column {
    @Id
    private String id; // MongoDB 기본 ID
    private Integer projectIdx; // 프로젝트 ID
    private String type; // Node 또는 Edge의 타입
    private List<ColumnDetail> columnDetailList; // 컬럼 세부 정보 리스트
}
