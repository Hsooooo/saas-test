package com.illunex.emsaasrestapi.query;

import com.illunex.emsaasrestapi.member.vo.MemberVO;
import com.illunex.emsaasrestapi.query.dto.RequestQueryDTO;
import com.mongodb.MongoException;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.bson.json.JsonParseException;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QueryService {
    private final QueryComponent queryComponent;
    private final MongoTemplate mongoTemplate;

    public Object executeQuery(MemberVO memberVO, RequestQueryDTO.ExecuteQuery executeQuery) {
        QueryResult queryResult = resolveQuery(executeQuery);
        List<Map> results = mongoTemplate.find(queryResult.query(), Map.class, queryResult.collection());
        long total = mongoTemplate.count(Query.of(queryResult.query()).limit(0).skip(0), queryResult.collection());
        return ResponseEntity.ok(Map.of(
                "total", total,
                "page", executeQuery.getPage(),
                "size", executeQuery.getSize(),
                "result", results
        ));
    }

    public QueryResult resolveQuery(RequestQueryDTO.ExecuteQuery req) {
        try {
            // 1. 필수 필드: projectIdx
            Integer projectIdx = req.getProjectIdx();
            if (projectIdx == null) throw new IllegalArgumentException("projectIdx는 필수입니다.");

            // 2. 필수 필드: filter
            if (req.getFilter() == null || req.getFilter().isBlank())
                throw new IllegalArgumentException("filter는 JSON 문자열 형식으로 필수입니다.");

            Document filter = Document.parse(req.getFilter());

            // 3. table 추출
            String table = filter.getString("table");
            if (table == null || table.isBlank())
                throw new IllegalArgumentException("filter 내에 table 필드가 필요합니다.");
            filter.remove("table");

            // 4. 실제 컬렉션 판별
            boolean isNode = mongoTemplate.exists(
                    Query.query(Criteria.where("_id.projectIdx").is(projectIdx).and("label").is(table)),
                    "node"
            );

            String collection;
            String tableField;
            if (isNode) {
                collection = "node";
                tableField = "label";
            } else {
                boolean isEdge = mongoTemplate.exists(
                        Query.query(Criteria.where("_id.projectIdx").is(projectIdx).and("type").is(table)),
                        "edge"
                );
                if (!isEdge) {
                    throw new IllegalArgumentException("해당 테이블은 존재하지 않습니다: " + table);
                }
                collection = "edge";
                tableField = "type";
            }

            // 5. MongoDB 쿼리 생성
            Query query = new Query();
            query.addCriteria(Criteria.where("_id.projectIdx").is(projectIdx));
            query.addCriteria(Criteria.where(tableField).is(table));

            // 6. properties 필터 처리
            for (Map.Entry<String, Object> entry : filter.entrySet()) {
                query.addCriteria(Criteria.where("properties." + entry.getKey()).is(entry.getValue()));
            }

            // 7. projection
            if (req.getProjection() != null && !req.getProjection().isBlank()) {
                Document proj = Document.parse(req.getProjection());
                for (String key : proj.keySet()) {
                    query.fields().include("properties." + key);
                }
            }

            // 8. sort
            if (req.getSort() != null && !req.getSort().isBlank()) {
                Document sortDoc = Document.parse(req.getSort());
                List<Sort.Order> orders = sortDoc.entrySet().stream()
                        .map(e -> new Sort.Order(
                                ((Number) e.getValue()).intValue() == 1 ? Sort.Direction.ASC : Sort.Direction.DESC,
                                "properties." + e.getKey()))
                        .toList();
                query.with(Sort.by(orders));
            }

            // 9. paging
            int page = Optional.ofNullable(req.getPage()).orElse(0);
            int size = Optional.ofNullable(req.getSize()).orElse(50);
            query.skip((long) page * size);
            query.limit(size);

            return new QueryResult(query, collection);
        } catch (JsonParseException | MongoException e) {
            throw new RuntimeException("Mongo 쿼리 변환 오류: " + e.getMessage());
        }
    }

    public record QueryResult(Query query, String collection) {}

}
