package com.illunex.emsaasrestapi.query;

import lombok.RequiredArgsConstructor;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class SqlToMongoAdapter {

    private final Set<String> allowedTables;
    private final Set<String> nodeTables;
    private final Set<String> edgeTables;
    private final int limitMax;     // 예: 2000
    private final int limitDefault; // 예: 50

    /**
     * SQL 문자열을 받아 Mongo Query로 변환
     */
    public QueryResult resolve(Integer projectIdx, String sql, Integer uiSkip, Integer uiLimit) {
        if (projectIdx == null) throw new IllegalArgumentException("projectIdx는 필수입니다.");
        if (sql == null || sql.isBlank()) throw new IllegalArgumentException("sql은 비어있을 수 없습니다.");

        QueryDsl dsl = this.parseToDsl(sql);

        // 우선순위: SQL > UI
        int resolvedLimit = mergeLimit(dsl.getLimit(), uiLimit);
        int resolvedOffset = mergeOffset(dsl.getOffset(), uiSkip);

        return dslToMongoQuery(dsl, projectIdx, resolvedLimit, resolvedOffset);
    }

    private int mergeLimit(Integer sqlLimit, Integer uiLimit) {
        Integer base = (sqlLimit != null) ? sqlLimit : uiLimit;
        if (base == null || base <= 0) base = limitDefault;
        return Math.min(base, limitMax);
    }

    private int mergeOffset(Integer sqlOffset, Integer uiSkip) {
        Integer base = (sqlOffset != null) ? sqlOffset : uiSkip;
        if (base == null || base < 0) base = 0;
        return base;
    }

    private QueryResult dslToMongoQuery(QueryDsl dsl, Integer projectIdx, int limit, int offset) {
        String table = dsl.getTable();
        String collection;
        String tableField;
        if (nodeTables.contains(table)) {
            collection = "node";
            tableField = "label";
        } else if (edgeTables.contains(table)) {
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

        // Sort (없으면 _id ASC)
        if (dsl.getOrderBy() != null && !dsl.getOrderBy().isEmpty()) {
            List<Sort.Order> orders = dsl.getOrderBy().stream()
                    .map(o -> new Sort.Order(o.isAsc() ? Sort.Direction.ASC : Sort.Direction.DESC,
                            "properties." + o.getCol()))
                    .toList();
            query.with(Sort.by(orders));
        } else {
            query.with(Sort.by(Sort.Direction.ASC, "_id"));
        }

        // Paging
        query.limit(limit);
        query.skip(offset);

        return new QueryResult(query, collection);
    }

    private QueryResult dslToMongoQuery(QueryDsl dsl, Integer projectIdx) {
        String table = dsl.getTable();
        String collection;
        String tableField;
        if (nodeTables.contains(table)) {
            collection = "node";
            tableField = "label";
        } else if (edgeTables.contains(table)) {
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



    public QueryDsl parseToDsl(String sql) {
        Statement stmt;
        try {
            stmt = CCJSqlParserUtil.parse(sql);
        } catch (Exception e) {
            throw new IllegalArgumentException("SQL 파싱 실패: " + e.getMessage(), e);
        }
        if (!(stmt instanceof Select select)) {
            throw new IllegalArgumentException("SELECT 쿼리만 허용됩니다.");
        }
        return toDsl(select);
    }

    private QueryDsl toDsl(Select select) {
        if (!(select.getSelectBody() instanceof PlainSelect ps)) {
            throw new IllegalArgumentException("지원하지 않는 SELECT 형태입니다.");
        }

        // FROM
        if (!(ps.getFromItem() instanceof Table t)) {
            throw new IllegalArgumentException("FROM에는 테이블만 허용됩니다.");
        }
        String table = normalizeName(t.getName());
        if (!allowedTables.contains(table)) {
            throw new IllegalArgumentException("허용되지 않은 테이블: " + table);
        }
        if (ps.getJoins() != null && !ps.getJoins().isEmpty()) {
            throw new IllegalArgumentException("JOIN은 현재 허용되지 않습니다.");
        }

        // SELECT 컬럼
        List<String> columns;
        // 1) select * 또는 t.* 만 단독으로 오는 경우 → 전체 반환(프로젝션 X)
        if (ps.getSelectItems().size() == 1 &&
                (ps.getSelectItems().get(0) instanceof AllColumns
                        || ps.getSelectItems().get(0) instanceof AllTableColumns)) {
            columns = List.of(); // ← 빈 리스트면 어댑터에서 projection 미적용(전체 필드)
        } else {
            // 2) 명시 컬럼 나열
            columns = ps.getSelectItems().stream().map(si -> {
                if (si instanceof AllColumns || si instanceof AllTableColumns) {
                    // 혼합 사용: id, *, name 같은 케이스는 허용하지 않음(정책상 금지)
                    throw new IllegalArgumentException("SELECT * 는 단독으로만 허용됩니다.");
                }
                if (si instanceof SelectExpressionItem sei) {
                    Expression e = sei.getExpression();
                    // (함수 제한 필요 시 여기서 체크)
                    return normalizeName(e.toString());
                }
                throw new IllegalArgumentException("지원하지 않는 SELECT 항목: " + si);
            }).collect(Collectors.toList());
        }

        // WHERE
        QueryDsl.FilterExpr where = (ps.getWhere() == null)
                ? new QueryDsl.TrueExpr()
                : toFilterExpr(ps.getWhere());

        // ORDER BY
        List<QueryDsl.Order> order = Optional.ofNullable(ps.getOrderByElements()).orElse(List.of()).stream()
                .map(o -> QueryDsl.Order.builder()
                        .col(normalizeName(o.getExpression().toString()))
                        .asc(Boolean.TRUE.equals(o.isAsc()))
                        .build())
                .toList();

        // LIMIT / OFFSET / FETCH
        Integer limit = null;
        Integer offset = 0;
        if (ps.getLimit() != null) {
            Limit l = ps.getLimit();
            if (l.getRowCount() != null) limit = Integer.parseInt(l.getRowCount().toString());
            if (l.getOffset() != null) offset = Integer.parseInt(l.getOffset().toString());
        }
        if (limit == null && ps.getFetch() != null) { // e.g., FETCH FIRST 100 ROWS ONLY
            limit = (int) ps.getFetch().getRowCount();
        }
        if (limit == null) limit = limitDefault;
        limit = Math.min(limit, limitMax);

        return QueryDsl.builder()
                .table(table)
                .columns(columns)  // ← 비어 있으면 전체 반환
                .where(where)
                .orderBy(order)
                .limit(limit)
                .offset(offset)
                .build();
    }

    private String normalizeName(String s) {
        // 컬럼/테이블 토큰을 단순 정규화(백틱/따옴표 제거, 별칭 미사용 권장)
        if (s == null) return null;
        return s.replace("`","").replace("\"","").trim();
    }

    private QueryDsl.FilterExpr toFilterExpr(Expression e) {
        // 논리식
        if (e instanceof AndExpression a) {
            return QueryDsl.Bin.builder().op("AND")
                    .left(toFilterExpr(a.getLeftExpression()))
                    .right(toFilterExpr(a.getRightExpression()))
                    .build();
        }
        if (e instanceof OrExpression o) {
            return QueryDsl.Bin.builder().op("OR")
                    .left(toFilterExpr(o.getLeftExpression()))
                    .right(toFilterExpr(o.getRightExpression()))
                    .build();
        }
        if (e instanceof Parenthesis p) {
            return QueryDsl.Group.builder().expr(toFilterExpr(p.getExpression())).build();
        }
        if (e instanceof NotExpression n) {
            return QueryDsl.Unary.builder().op("NOT").expr(toFilterExpr(n.getExpression())).build();
        }

        // 비교/IN/BETWEEN/LIKE/IS NULL
        if (e instanceof ComparisonOperator cmp) {
            String op = cmp.getStringExpression();
            String col = normalizeName(cmp.getLeftExpression().toString());
            Object val = literalValueOf(cmp.getRightExpression());
            return QueryDsl.Predicate.builder().col(col).op(op).val(val).build();
        }
        if (e instanceof InExpression in) {
            String col = normalizeName(in.getLeftExpression().toString());
            List<Object> vals = ((ExpressionList) in.getRightItemsList()).getExpressions()
                    .stream().map(this::literalValueOf).toList();
            return QueryDsl.Predicate.builder().col(col).op("IN").val(vals).build();
        }
        if (e instanceof Between between) {
            String col = normalizeName(between.getLeftExpression().toString());
            Object from = literalValueOf(between.getBetweenExpressionStart());
            Object to = literalValueOf(between.getBetweenExpressionEnd());
            return QueryDsl.Predicate.builder().col(col).op("BETWEEN").val(List.of(from, to)).build();
        }
        if (e instanceof LikeExpression like) {
            String col = normalizeName(like.getLeftExpression().toString());
            Object pat = literalValueOf(like.getRightExpression());
            return QueryDsl.Predicate.builder().col(col).op("LIKE").val(pat).build();
        }
        if (e instanceof IsNullExpression isnull) {
            String col = normalizeName(isnull.getLeftExpression().toString());
            return QueryDsl.Predicate.builder().col(col)
                    .op(isnull.isNot() ? "IS NOT NULL" : "IS NULL").build();
        }

        throw new IllegalArgumentException("지원하지 않는 WHERE 표현식: " + e);
    }

    private Object literalValueOf(Expression e) {
        if (e instanceof StringValue sv) return sv.getValue();
        if (e instanceof LongValue lv) return lv.getValue();
        if (e instanceof DoubleValue dv) return dv.getValue();
        if (e instanceof DateValue dv) return dv.getValue();
        if (e instanceof TimeValue tv) return tv.getValue();
        if (e instanceof TimestampValue ts) return ts.getValue();
        // 심볼/식/컬럼 비교의 오른쪽이 또 컬럼이면 금지
        throw new IllegalArgumentException("리터럴만 허용됩니다: " + e);
    }
}