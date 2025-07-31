package com.illunex.emsaasrestapi.query;

import com.illunex.emsaasrestapi.query.dto.RequestQueryDTO;
import com.mongodb.MongoException;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.bson.json.JsonParseException;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class QueryComponent {
    private final MongoTemplate mongoTemplate;

    public Object executeFind(RequestQueryDTO.ExecuteQuery req) {
        try {
            Document filter = Document.parse(req.getFilter());
            Document projection = req.getProjection() != null && !req.getProjection().isBlank()
                    ? Document.parse(req.getProjection())
                    : null;

            Query query;
            if (projection == null) {
                query = new BasicQuery(filter);
                query.addCriteria(Criteria.where("_id.projectIdx").is(req.getProjectIdx()));
            } else {
                query = new BasicQuery(filter, projection);
                query.addCriteria(Criteria.where("_id.projectIdx").is(req.getProjectIdx()));
            }


            if (req.getSort() != null && !req.getSort().isBlank()) {
                Document sortDoc = Document.parse(req.getSort());
                List<Sort.Order> orders = sortDoc.entrySet().stream()
                        .map(e -> new Sort.Order(
                                ((Number) e.getValue()).intValue() == 1 ? Sort.Direction.ASC : Sort.Direction.DESC,
                                e.getKey()))
                        .toList();
                query.with(Sort.by(orders));
            }

            int page = req.getPage() != null && req.getPage() >= 0 ? req.getPage() : 0;
            int size = req.getSize() != null && req.getSize() > 0 ? req.getSize() : 50;
            int skip = req.getSkip() != null && req.getSkip() >= 0 ? req.getSkip() : 0;
            int limit = req.getLimit() != null && req.getLimit() > 0 ? req.getLimit() : 50;

            query.skip(skip);
            query.limit(limit);

            List<Map> result = mongoTemplate.find(query, Map.class, req.getCollection());
            long total = mongoTemplate.count(Query.of(query).limit(0).skip(0), req.getCollection());

            return ResponseEntity.ok(Map.of(
                    "total", total,
                    "page", page,
                    "size", size,
                    "result", result
            ));
        } catch (JsonParseException | MongoException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Mongo 쿼리 실행 오류: " + e.getMessage());
        }
    }

//    public Object executeAggregate(RequestQueryDTO.ExecuteQuery executeQuery) {
//        // Logic to execute an aggregate operation
//        // This is a placeholder for actual implementation
//        System.out.println("Executing aggregate query: " + executeQuery.getQuery() + " for project: " + executeQuery.getProjectIdx());
//        return null; // Return appropriate response or object
//    }
//
//
//    public Object executeInsert(RequestQueryDTO.ExecuteQuery executeQuery) {
//        // Logic to execute an insert operation
//        // This is a placeholder for actual implementation
//        System.out.println("Executing insert query: " + executeQuery.getQuery() + " for project: " + executeQuery.getProjectIdx());
//        return null; // Return appropriate response or object
//    }
}
