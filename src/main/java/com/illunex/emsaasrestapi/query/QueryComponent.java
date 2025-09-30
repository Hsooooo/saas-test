package com.illunex.emsaasrestapi.query;

import com.illunex.emsaasrestapi.common.code.EnumCode;
import com.illunex.emsaasrestapi.project.mapper.ProjectTableMapper;
import com.illunex.emsaasrestapi.project.vo.ProjectTableVO;
import com.illunex.emsaasrestapi.query.dto.RequestQueryDTO;
import com.illunex.emsaasrestapi.query.dto.ResponseQueryDTO;
import com.mongodb.MongoException;
import lombok.RequiredArgsConstructor;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import org.bson.Document;
import org.bson.json.JsonParseException;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class QueryComponent {
    private final MongoTemplate mongoTemplate;
    private final ProjectTableMapper projectTableMapper;

    /**
     * MongoDB 쿼리 변환
     *
     * @param req 쿼리 요청 DTO
     * @return QueryResult 객체, 쿼리와 컬렉션 정보 포함
     */
    public QueryResult resolveQuery(RequestQueryDTO.FindQuery req) {
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
            int skip = Optional.ofNullable(req.getSkip()).orElse(0);
            int limit = Optional.ofNullable(req.getLimit()).orElse(10);
            query.skip(skip);
            query.limit(limit);

            return new QueryResult(query, collection);
        } catch (JsonParseException | MongoException e) {
            throw new RuntimeException("Mongo 쿼리 변환 오류: " + e.getMessage());
        }
    }

    public QueryResult executeQuery(RequestQueryDTO.FindQuery req) {
        QueryResult queryResult = resolveQuery(req);

        return queryResult;
    }

    public QueryResult resolveSql(RequestQueryDTO.ExecuteQuery req) {
        Integer projectIdx = Objects.requireNonNull(req.getProjectIdx(), "projectIdx는 필수입니다.");
        List<ProjectTableVO> tables = projectTableMapper.selectAllByProjectIdx(projectIdx);
        Set<String> allowedTables = tables.stream()
                .map(ProjectTableVO::getTitle)
                .collect(Collectors.toSet());
        Set<String> nodeTables = tables.stream()
                .filter(t -> EnumCode.ProjectTable.TypeCd.Node.getCode().equals(t.getTypeCd())) // Node
                .map(ProjectTableVO::getTitle)
                .collect(Collectors.toSet());
        Set<String> edgeTables = tables.stream()
                .filter(t -> EnumCode.ProjectTable.TypeCd.Edge.getCode().equals(t.getTypeCd())) // Edge
                .map(ProjectTableVO::getTitle)
                .collect(Collectors.toSet());

        String sql = Optional.ofNullable(req.getRawQuery()).orElseThrow(() -> new IllegalArgumentException("sql은 필수입니다.")).trim();
        if (sql.isEmpty()) throw new IllegalArgumentException("sql은 비어있을 수 없습니다.");
        SqlToMongoAdapter converter = new SqlToMongoAdapter(allowedTables, nodeTables, edgeTables, 5000, 50);

        return converter.resolve(projectIdx, sql);

//        List<Map> results = mongoTemplate.find(queryResult.query(), Map.class, queryResult.collection());
//        return null;
//        long total = mongoTemplate.count(Query.of(queryResult.query()).limit(0).skip(0), queryResult.collection());
//        int page = (executeQuery.getSkip() / executeQuery.getLimit()) + 1;
//        int size = executeQuery.getLimit();
//        return ResponseEntity.ok(ResponseQueryDTO.ExecuteFind.builder()
//                .total(total)
//                .page(page)
//                .size(size)
//                .skip(executeQuery.getSkip())
//                .limit(size)
//                .result(results)
//                .build());
//
//        // 1) 파싱
//        Statement stmt;
//        try {
//            stmt = CCJSqlParserUtil.parse(sql);
//        } catch (JSQLParserException e) {
//            throw new IllegalArgumentException("SQL 파싱 실패: " + e.getMessage());
//        }
//
//        // 2) SELECT 서브셋만 허용
//        if (!(stmt instanceof Select select)) {
//            throw new IllegalArgumentException("SELECT 쿼리만 허용됩니다.");
//        }
//
//        // 3) AST → 내부 DSL
////        SqlToDslConverter converter = new SqlToDslConverter(allowedTables(), allowedFunctions());
//        QueryDsl dsl = converter.toDsl(select);
//
//        // 4) DSL → Mongo Query (node/edge 판별 및 properties.* 접두 추가)
//        return dslToMongoQuery(dsl, projectIdx);
//        return null;
    }


//    public ExecuteSqlResponse execute(Integer projectIdx, String sql) {
//        long t0 = System.currentTimeMillis();
//
//        // 2) SQL → (DSL) → Mongo Query/collection
//        SqlToMongoAdapter.QueryResult qr = sqlToMongoAdapter.resolve(projectIdx, sql);
//
//        // 3) 실행 (타임아웃/제한)
//        //   - Mongo Java Driver 수준에서 maxTimeMS 를 적용하려면 Query에 hint 불가 → Template 수준보단 Repository/Driver 옵션에서 설정 고려
//        List<Document> docs = mongoTemplate.find(qr.query(), Document.class, qr.collection());
//
//        // 4) projection 결과 평탄화(properties.* → 1뎁스)
//        List<Map<String, Object>> rows = projectionFlattener.flatten(docs);
//
//        // 5) (옵션) 총건수
//        Long total = null; // 필요 시 카운트: mongoTemplate.count(qr.query().skip(0).limit(0), qr.collection())
//
//        long took = System.currentTimeMillis() - t0;
//        auditLogger.log(projectIdx, sql, qr, docs.size(), took);  // 선택
//
//        return ExecuteSqlResponse.builder()
//                .collection(qr.collection())
//                .total(total)
//                .rows(rows)
//                .build();
//    }
}
