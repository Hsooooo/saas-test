package com.illunex.emsaasrestapi.hts.repository;

import com.illunex.emsaasrestapi.hts.dto.ResponseHtsDTO;
import com.illunex.emsaasrestapi.hts.entity.InvestorTrends;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.illunex.emsaasrestapi.hts.entity.QInvestorTrends.investorTrends;

@Repository
public class InvestorTrendsCustomRepository {
    private final JPAQueryFactory query;

    public InvestorTrendsCustomRepository(JPAQueryFactory query, JPAQueryFactory jpaQueryFactory) {
        this.query = query;
    }

    // 투자자 동향에 중복된 날짜가 있을 경우 최신 데이터로 가져온다.
    public Page<InvestorTrends> findByInvestorTrends(Pageable pageable, String iscd) {
        // 서브쿼리로 최신의 이름 리스트를 가져온다.
        List<Integer> subQuery = query
                .select(investorTrends.idx.max())
                .from(investorTrends)
                .where(investorTrends.iscd.eq(iscd))
                .groupBy(investorTrends.iscd, investorTrends.investorDate)
                .fetch();
        List<InvestorTrends> response = query
                .select(investorTrends)
                .from(investorTrends)
                .where(investorTrends.iscd.eq(iscd)
                        .and(investorTrends.idx.in(subQuery))
                )
                .orderBy(investorTrends.investorDate.desc())
                .fetch();

        // Page 객체로 변환
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), response.size());
        return new PageImpl<>(response.subList(start, end), pageable, response.size());
    }

    /**
     * 종목별 투자자 동향 - 기간별 거래량 차트(6개만 보임)
     * @param startDay
     * @param endDay
     * @param iscd
     * @return
     */
    public ResponseHtsDTO.TradingVolumChart findByVolumData(LocalDate startDay, LocalDate endDay, String iscd) {
        List<Integer> subQuery = query
                .select(investorTrends.idx.max())
                .from(investorTrends)
                .where(
                        investorTrends.iscd.eq(iscd)
                        .and(investorTrends.investorDate.between(startDay.format(DateTimeFormatter.ofPattern("yyyyMMdd")), endDay.format(DateTimeFormatter.ofPattern("yyyyMMdd"))))
                )
                .groupBy(investorTrends.investorDate)
                .fetch();
        ResponseHtsDTO.TradingVolumChart response = query.selectFrom(investorTrends)
                .select(Projections.fields(
                        ResponseHtsDTO.TradingVolumChart.class,
                        investorTrends.netForeignBuyingVolume.sum().as("foreignVolume"),
                        investorTrends.individualNetBuyVolume.sum().as("individualVolume"),
                        investorTrends.institutionalNetBuyVolume.sum().as("institutionalVolume")
                ))
                .from(investorTrends)
                .where(investorTrends.idx.in(subQuery))
                .fetchOne();
        return response;
    }
}
