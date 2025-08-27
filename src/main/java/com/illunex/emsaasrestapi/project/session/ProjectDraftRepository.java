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