package com.illunex.emsaasrestapi.ai.repository;

import com.illunex.emsaasrestapi.ai.dto.ResponseAiMapper;
import com.illunex.emsaasrestapi.ai.entity.AiDartAnalysis;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AiDartAnalysisRepository extends JpaRepository<AiDartAnalysis, Long> {
    int countByStockCodeAndRceptDtGreaterThanEqualAndRceptDtLessThanEqual(String stockCode, LocalDate pre, LocalDate now);
    int countByStockCodeAndSentimentAndRceptDtGreaterThanEqualAndRceptDtLessThanEqual(String stockCode, String sentiment, LocalDate pre, LocalDate now);

    @Query(value = "select\n" +
            "    date_format(rcept_dt, '%Y-%m')as yearMonth,\n" +
            "    sentiment,\n" +
            "    count(*) as cnt from ai_dart_analysis\n" +
            "where stock_code= :stockCode and rcept_dt >= :pre and rcept_dt <= :now and sentiment = :sentiment\n" +
            "group by sentiment, yearMonth\n" +
            "order by yearMonth desc\n" +
            "limit 12"
            ,nativeQuery = true)
    List<ResponseAiMapper.AiDartAnalysisChartInterface> findTop12ByStockCodeAndSentimentAndRceptDtOrderByRceptDtDesc(String stockCode, LocalDate now, LocalDate pre, String sentiment);
    Page<AiDartAnalysis> findByStockCodeAndRceptDtGreaterThanEqualAndRceptDtLessThanEqualOrderByRceptDtDesc(String stockCode, LocalDate pre, LocalDate now, Pageable pageable);
}