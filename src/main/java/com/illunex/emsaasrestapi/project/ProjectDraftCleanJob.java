package com.illunex.emsaasrestapi.project;

import com.illunex.emsaasrestapi.common.aws.AwsS3Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.domain.Sort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class ProjectDraftCleanJob {
    private final String DELIMITER = "/";
    @Value("${spring.config.activate.on-profile}")
    private String folderName;

    private final MongoTemplate mongoTemplate;
    private final AwsS3Component awsS3Component;

    private static final int STALE_HOURS = 24;
    private static final int BATCH = 1000;
    private static final String COLLECTION = "project_drafts";

    @Scheduled(cron = "0 45 3 * * *", zone = "Asia/Seoul") // 매일 03:45 KST
//    @Scheduled(cron = "*/10 * * * * *", zone = "Asia/Seoul") // TEST
    public void purge() {
        final LocalDateTime threshold = LocalDateTime.now().minusHours(STALE_HOURS);

        while (true) {
            Query q = Query.query(Criteria.where("updatedAt").lte(threshold))
                    .with(Sort.by(Sort.Direction.ASC, "updatedAt"))
                    .limit(BATCH);
            q.fields()
                    .include("_id")
                    .include("s3Keys") // 주 저장소
                    .include("excelMeta.excelFileList.filePath")  // 폴백
                    .include("excelMeta.excelSheetList.filePath"); // 폴백

            List<Document> stale = mongoTemplate.find(q, Document.class, COLLECTION);
            if (stale.isEmpty()) break;

            List<ObjectId> ids = new ArrayList<>(stale.size());
            int s3Deleted = 0;

            for (Document d : stale) {
                ObjectId sid = d.getObjectId("_id");
                ids.add(sid);

                // 키 수집
                Set<String> keys = new LinkedHashSet<>();

                // 1) s3Keys 최우선
                @SuppressWarnings("unchecked")
                List<String> s3Keys = (List<String>) d.get("s3Keys");
                if (s3Keys != null) keys.addAll(s3Keys);

                // 2) (폴백) excelMeta에서 키 뽑기
                Document excelMeta = (Document) d.get("excelMeta");
                if (excelMeta != null) {
                    @SuppressWarnings("unchecked")
                    List<Document> files =
                            (List<Document>) excelMeta.get("excelFileList");
                    if (files != null) {
                        for (var f : files) {
                            var fp = (String) f.get("filePath");
                            if (fp != null && !fp.isBlank()) keys.add(fp);
                        }
                    }

                    @SuppressWarnings("unchecked")
                    List<Document> sheets =
                            (List<Document>) excelMeta.get("excelSheetList");
                    if (sheets != null) {
                        for (var s : sheets) {
                            var fp = (String) s.get("filePath");
                            if (fp != null && !fp.isBlank()) keys.add(fp);
                        }
                    }
                }

                // 삭제
                try {
                    s3Deleted += awsS3Component.deleteObjects(keys);
                } catch (Exception e) {
                    log.warn("[draft-clean] sid={} deleteObjects err={}", sid, e.toString());
                }
            }

            long mongoDeleted = mongoTemplate
                    .remove(Query.query(Criteria.where("_id").in(ids)), COLLECTION)
                    .getDeletedCount();

            log.info("[draft-clean] mongoDeleted={} s3ObjectsDeleted={} threshold={}",
                    mongoDeleted, s3Deleted, threshold);
        }
    }
}
