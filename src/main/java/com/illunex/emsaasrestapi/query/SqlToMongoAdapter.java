package com.illunex.emsaasrestapi.query;

import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.*;

@RequiredArgsConstructor
public class SqlToMongoAdapter {

    public record QueryResult(Query query, String collection) { }

    public interface TableCatalog {
        boolean isNodeTable(String table);
        boolean isEdgeTable(String table);
    }

    private final TableCatalog catalog;
    private final SqlToDslConverter converter;

    /**
     * SQL 문자열을 받아 Mongo Query로 변환
     */
    public QueryResult resolve(Integer projectIdx, String sql) {
        if (projectIdx == null) throw new IllegalArgumentException("projectIdx는 필수입니다.");
        if (sql == null || sql.isBlank()) throw new IllegalArgumentException("sql은 비어있을 수 없습니다.");

        QueryDsl dsl = converter.parseToDsl(sql);
        return dslToMongoQuery(dsl, projectIdx);
    }

    private QueryResult dslToMongoQuery(QueryDsl dsl, Integer projectIdx) {
        String table = dsl.getTable();
        String collection;
        String tableField;
        if (catalog.isNodeTable(table)) {
            collection = "node";
            tableField = "label";
        } else if (catalog.isEdgeTable(table)) {
            collection = "edge";
            tableField = "type";
        } else {
            throw new IllegalArgumentException("알 수 없는 테이블: " + table);
        }

        Query query = new Query();
        query.addCriteria(Criteria.where("_id.projectIdx").is(projectIdx));
        query.addCriteria(Criteria.where(tableField).is(table));

        // WHERE
        Criteria where = toCriteria(dsl.getWhere());
        if (where != null) query.addCriteria(where);

        // Projection
        if (dsl.getColumns() != null && !dsl.getColumns().isEmpty()) {
            for (String c : dsl.getColumns()) {
                query.fields().include("properties." + c);
            }
        }

        // Sort
        if (dsl.getOrderBy() != null && !dsl.getOrderBy().isEmpty()) {
            List<Sort.Order> orders = dsl.getOrderBy().stream()
                    .map(o -> new Sort.Order(o.isAsc() ? Sort.Direction.ASC : Sort.Direction.DESC,
                            "properties." + o.getCol()))
                    .toList();
            query.with(Sort.by(orders));
        }

        // Paging
        query.limit(dsl.getLimit());
        query.skip(Optional.ofNullable(dsl.getOffset()).orElse(0));

        return new QueryResult(query, collection);
    }

    private Criteria toCriteria(QueryDsl.FilterExpr expr) {
        if (expr == null || expr instanceof QueryDsl.TrueExpr) return null;

        if (expr instanceof QueryDsl.Group g) {
            return toCriteria(g.expr());
        }
        if (expr instanceof QueryDsl.Unary u) {
            if (!"NOT".equalsIgnoreCase(u.op())) {
                throw new IllegalArgumentException("지원하지 않는 단항 연산자: " + u.op());
            }

            if (u.expr() instanceof QueryDsl.Predicate p) {
                String path = "properties." + p.getCol();
                return switch (p.getOp().toUpperCase(Locale.ROOT)) {
                    case "="  -> Criteria.where(path).ne(p.getVal());
                    case "!=","<>" -> Criteria.where(path).is(p.getVal()); // NOT (col != v) == col = v
                    case "IS NULL"     -> Criteria.where(path).ne(null);
                    case "IS NOT NULL" -> Criteria.where(path).is(null);
                    default -> throw new IllegalArgumentException("해당 연산자는 NOT을 지원하지 않습니다: " + p.getOp());
                };
            }

            throw new IllegalArgumentException("NOT은 단일 Predicate에만 적용 가능합니다.");
        }
        if (expr instanceof QueryDsl.Bin b) {
            Criteria left = toCriteria(b.left());
            Criteria right = toCriteria(b.right());
            return switch (b.op().toUpperCase(Locale.ROOT)) {
                case "AND" -> new Criteria().andOperator(left, right);
                case "OR"  -> new Criteria().orOperator(left, right);
                default -> throw new IllegalArgumentException("지원하지 않는 이항 연산자: " + b.op());
            };
        }
        if (expr instanceof QueryDsl.Predicate p) {
            String path = "properties." + p.getCol();
            String op = p.getOp().toUpperCase(Locale.ROOT);
            return switch (op) {
                case "=" -> Criteria.where(path).is(p.getVal());
                case "!=", "<>" -> Criteria.where(path).ne(p.getVal());
                case ">"  -> Criteria.where(path).gt(p.getVal());
                case ">=" -> Criteria.where(path).gte(p.getVal());
                case "<"  -> Criteria.where(path).lt(p.getVal());
                case "<=" -> Criteria.where(path).lte(p.getVal());
                case "IN" -> Criteria.where(path).in((Collection<?>) p.getVal());
                case "BETWEEN" -> {
                    List<?> v = (List<?>) p.getVal();
                    yield Criteria.where(path).gte(v.get(0)).lte(v.get(1));
                }
                case "LIKE" -> {
                    // 보안/성능 규칙: 접두 검색만 허용 ('abc%')
                    String pat = String.valueOf(p.getVal());
                    if (pat.endsWith("%") && !pat.startsWith("%")) {
                        String prefix = pat.substring(0, pat.length()-1)
                                .replace("\\", "\\\\")
                                .replace(".", "\\.")
                                .replace("+", "\\+")
                                .replace("?", "\\?")
                                .replace("*", "\\*")
                                .replace("[", "\\[")
                                .replace("]", "\\]");
                        yield Criteria.where(path).regex("^" + prefix + ".*");
                    }
                    throw new IllegalArgumentException("LIKE는 접두 검색만 허용됩니다 (예: 'abc%').");
                }
                case "IS NULL"     -> Criteria.where(path).is(null);
                case "IS NOT NULL" -> Criteria.where(path).ne(null);
                default -> throw new IllegalArgumentException("지원하지 않는 연산자: " + p.getOp());
            };
        }
        throw new IllegalArgumentException("알 수 없는 WHERE 노드");
    }
}