package com.illunex.emsaasrestapi.project.session;


import com.illunex.emsaasrestapi.project.dto.RequestProjectDTO;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.*;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@RequiredArgsConstructor
public class ProjectDraftRepository {
    private final MongoTemplate mongo;

    public ObjectId open(ObjectId sessionId, Long memberId, RequestProjectDTO.Project p) {
        ObjectId sid = (sessionId != null) ? sessionId : new ObjectId();
        // status=OPEN, 생성자/파트너십/카테고리 등 최소 메타 저장
        upsert(sid, new Update()
                .setOnInsert("status", "OPEN")
                .setOnInsert("ownerMemberId", memberId)
                .set("partnershipIdx", p.getPartnershipIdx())
                .set("projectCategoryIdx", p.getProjectCategoryIdx())
                .set("title", p.getTitle())
                .set("description", p.getDescription())
                .set("imagePath", p.getImagePath())
                .set("imageUrl", p.getImageUrl())
        );
        return sid;
    }

    public void ensureOpen(ObjectId sid, Long ownerId, RequestProjectDTO.Project project) {
        var existing = mongo.findById(sid, ProjectDraft.class);
        if (existing == null) {
            var now = new Date();
            var d = ProjectDraft.builder()
                    .id(sid).ownerId(ownerId).projectIdx(project.getProjectIdx()).title(project.getTitle()).description(project.getDescription())
                    .status("OPEN").createdAt(now).updatedAt(now)
                    .build();
            mongo.insert(d);
        }
    }

    public void upsert(ObjectId sid, Update update) {
        update.set("updatedAt", new Date());
        mongo.upsert(Query.query(Criteria.where("_id").is(sid)), update, ProjectDraft.class);
    }

    public ProjectDraft get(ObjectId sid) {
        return mongo.findById(sid, ProjectDraft.class);
    }

    public void mark(ObjectId sid, String status) {
        mongo.updateFirst(Query.query(Criteria.where("_id").is(sid)),
                new Update().set("status", status).set("updatedAt", new Date()),
                ProjectDraft.class);
    }
}