package com.illunex.emsaasrestapi.hts.repository;

import com.illunex.emsaasrestapi.hts.dto.ResponseHtsDTO;
import com.illunex.emsaasrestapi.hts.entity.JSise;
import com.illunex.emsaasrestapi.hts.entity.QJSise;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.illunex.emsaasrestapi.hts.entity.QJSise.jSise;

@Repository
public class JsiseCustomRepository {
    private final JPAQueryFactory query;
    QJSise jsise2 = new QJSise("sub");

    public JsiseCustomRepository(JPAQueryFactory query, JPAQueryFactory subQuery, JPAQueryFactory query2) {
        this.query = query;
    }

    // 종목별 상세정보 - 차트 탭 주식 차트 정보 가져오기
    public List<JSise> findByJSiseChart(String iscd, Long cnt) {
        List<JSise> response = query
                .selectFrom(jSise)
                .where(jSise.iscd.eq(iscd).and(jSise.createDate.in(
                        JPAExpressions
                                .select(jSise.createDate.max())
                                .from(jSise)
                                .where(jSise.iscd.eq(iscd).and(jSise.bsopDate.eq(jSise.bsopDate)))
                                .groupBy(jSise.iscd, jSise.bsopDate)

                )))
                .orderBy(jSise.bsopDate.asc())
                .limit(cnt)
                .fetch();
        return response;
    }

    public Page<JSise> findAllByIscd(String iscd, Pageable pageable) {
        JPAQuery<JSise> jSiseList = query.selectFrom(jSise)
                .where(jSise.iscd.eq(iscd))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(jSise.bsopDate.desc(), jSise.bsopHour.desc());
        List<JSise> content = jSiseList.fetch();

        Long totalCount = query.select(jSise.count()).from(jSise)
                .where(jSise.iscd.eq(iscd))
                .fetchOne();
        return new PageImpl<>(content, pageable, totalCount);
    }

    public List<JSise> TopNfindByJSise(String iscd, Long cnt) {
        List<JSise> response = query
                .selectFrom(jSise)
                .where(jSise.iscd.eq(iscd))
                .orderBy(jSise.bsopDate.asc())
                .limit(cnt)
                .fetch();
        return response;
    }

    public Page<ResponseHtsDTO.JsiseChart> findAllJsiseChartByIscd(String iscd, Pageable pageable) {
        JPAQuery<ResponseHtsDTO.JsiseChart> jSiseList = query
                .select(Projections.constructor(ResponseHtsDTO.JsiseChart.class,
                        jSise.idx, jSise.iscd, jSise.bsopDate, jSise.prpr, jSise.oprc, jSise.hgpr, jSise.lwpr, jSise.mxpr, jSise.llam, jSise.acmlVol, jSise.prdyCtrt)
                )
                .from(jSise)
                .where(jSise.iscd.eq(iscd))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(jSise.bsopDate.desc(), jSise.bsopHour.desc());
        List<ResponseHtsDTO.JsiseChart> content = jSiseList.fetch();

        Long totalCount = query.select(jSise.count()).from(jSise)
                .where(jSise.iscd.eq(iscd))
                .fetchOne();
        return new PageImpl<>(content, pageable, totalCount);
    }

    // 스톡 페이지 종목별 차트 조회
    public List<ResponseHtsDTO.StockChartGroup> findStockChart(String iscd, Integer from, int cnt) {
        List<ResponseHtsDTO.StockChartGroup> result = query
                .select(Projections.bean(ResponseHtsDTO.StockChartGroup.class,
                        jSise.idx, jSise.iscd, jSise.bsopDate.as("bsopDate"), jSise.oprc, jSise.hgpr, jSise.lwpr,
                        jSise.prpr, jSise.prdyClpr, jSise.prdyCtrt,
                        jSise.mxpr, jSise.llam, jSise.acmlVol))
                .from(jSise)
                .where(jSise.iscd.eq(iscd).and(jSise.bsopDate.loe(from)))
                .limit(cnt)
                .orderBy(jSise.bsopDate.desc())
                .fetch();

        return result;
    }
}
