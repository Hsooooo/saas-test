package com.illunex.emsaasrestapi.project.session;

import com.illunex.emsaasrestapi.project.document.excel.Excel;
import com.illunex.emsaasrestapi.project.document.project.Project; // ← 네가 완성 때 저장하는 그 구조
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Document("project_drafts")
public class ProjectDraft {
    @Id private ObjectId id;          // == sessionId
    private String status;            // OPEN | COMMITTED | CANCELLED | EXPIRED
    private Long ownerId;

    private Integer projectIdx;       // 신규=null, 수정이면 대상 idx

    private String title;           // 프로젝트 제목
    private Integer partnershipIdx; // 파트너십
    private Integer projectCategoryIdx; // 프로젝트 카테고리
    private String description;     // 프로젝트 설명
    private String imagePath;      // 프로젝트 이미지 경로
    private String imageUrl;       // 프로젝트 이미지 URL

    /** ✅ 실제 저장할 Project 도큐 형태 그대로 보관 (네 예시 JSON 그대로) */
    private Project projectDoc;

    /** ✅ Excel 메타(=시트명/row수/헤더 & S3 path)만 저장 (로우 데이터는 S3에서 읽음) */
    private Excel excelMeta;

    @Indexed(expireAfterSeconds = 0)
    private Date expiresAt;           // TTL
    private Date createdAt;
    private Date updatedAt;
}