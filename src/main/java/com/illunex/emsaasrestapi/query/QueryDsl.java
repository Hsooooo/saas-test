package com.illunex.emsaasrestapi.query;


import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class QueryDsl {
    private String table;
    private List<String> columns;
    private FilterExpr where;
    private List<Order> orderBy;
    private Integer limit;
    private Integer offset;

    @Getter
    @Builder
    public static class Order {
        private String col;
        private boolean asc;
    }

    // ✅ Predicate 를 permits 에 포함
    public sealed interface FilterExpr
            permits Bin, Unary, Group, TrueExpr, Predicate {}

    @Builder
    public static final class Bin implements FilterExpr {
        private final String op;          // AND / OR
        private final FilterExpr left;
        private final FilterExpr right;

        public String op() { return op; }
        public FilterExpr left() { return left; }
        public FilterExpr right() { return right; }
    }

    @Builder
    public static final class Unary implements FilterExpr {
        private final String op;          // NOT
        private final FilterExpr expr;

        public String op() { return op; }
        public FilterExpr expr() { return expr; }
    }

    @Builder
    public static final class Group implements FilterExpr {
        private final FilterExpr expr;
        public FilterExpr expr() { return expr; }
    }

    // ✅ final 지정
    public static final class TrueExpr implements FilterExpr { }

    @Getter
    @Builder
    public static final class Predicate implements FilterExpr {
        private String col;   // 컬럼
        private String op;    // =, !=, >, >=, <, <=, IN, LIKE, BETWEEN, IS NULL, IS NOT NULL
        private Object val;   // 단일/리스트/(from,to)
    }
}