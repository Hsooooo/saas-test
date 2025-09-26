package com.illunex.emsaasrestapi.query;

import lombok.RequiredArgsConstructor;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;

import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.Fetch;
import net.sf.jsqlparser.statement.select.Limit;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;

import java.util.*;
import java.util.stream.Collectors;

import java.util.*;
import java.util.stream.Collectors;

/**
 * SQL(서브셋) → QueryDsl 변환기
 * - SELECT만 허용
 * - SELECT * 금지
 * - 함수 화이트리스트
 * - LIMIT 상한
 */
@RequiredArgsConstructor
public class SqlToDslConverter {

    private final Set<String> allowedTables;
    private final Set<String> allowedFunctions;
    private final int limitMax;     // 예: 2000
    private final int limitDefault; // 예: 50

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
        List<String> columns = ps.getSelectItems().stream().map(si -> {
            if (si instanceof AllColumns || si instanceof AllTableColumns) {
                throw new IllegalArgumentException("SELECT * 는 허용되지 않습니다.");
            }
            if (si instanceof SelectExpressionItem sei) {
                Expression e = sei.getExpression();
                // 함수 제한
                if (e instanceof Function f) {
                    String fname = f.getName() == null ? "" : f.getName().toUpperCase(Locale.ROOT);
                    if (!allowedFunctions.contains(fname)) {
                        throw new IllegalArgumentException("허용되지 않은 함수: " + f.getName());
                    }
                }
                return normalizeName(e.toString());
            }
            throw new IllegalArgumentException("지원하지 않는 SELECT 항목: " + si);
        }).collect(Collectors.toList());

        // WHERE
        QueryDsl.FilterExpr where = (ps.getWhere() == null) ? new QueryDsl.TrueExpr() : toFilterExpr(ps.getWhere());

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
                .columns(columns)
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
