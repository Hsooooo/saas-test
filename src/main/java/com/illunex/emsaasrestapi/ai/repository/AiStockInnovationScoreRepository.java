package com.illunex.emsaasrestapi.ai.repository;

import com.illunex.emsaasrestapi.ai.dto.ResponseAiMapper;
import com.illunex.emsaasrestapi.ai.entity.AiStockInnovationScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AiStockInnovationScoreRepository extends JpaRepository<AiStockInnovationScore, Long> {
    Optional<AiStockInnovationScore> findByStockCodeAndBaseYear(String stockCode, Integer baseYear);
    /**
     * 전체 최소/평균/최대 값 조회
     * @return
     */
    @Query("select " +
                "min(a.totalScore) as min, " +
                "avg(a.totalScore) as avg, " +
                "max(a.totalScore) as max " +
            "from AiStockInnovationScore a")
    ResponseAiMapper.AiInnovationAggregationInterface getAggregationAll();
    /**
     * 전체 분포도 조회(구분 단위 10점 ~ 최대 100점)
     * @return
     */
    @Query("select " +
                "(select count(a) from AiStockInnovationScore a where a.totalScore between 0 and 10) as score10, " +
                "(select count(a) from AiStockInnovationScore a where a.totalScore between 10 and 20) as score20, " +
                "(select count(a) from AiStockInnovationScore a where a.totalScore between 20 and 30) as score30, " +
                "(select count(a) from AiStockInnovationScore a where a.totalScore between 30 and 40) as score40, " +
                "(select count(a) from AiStockInnovationScore a where a.totalScore between 40 and 50) as score50, " +
                "(select count(a) from AiStockInnovationScore a where a.totalScore between 50 and 60) as score60, " +
                "(select count(a) from AiStockInnovationScore a where a.totalScore between 60 and 70) as score70, " +
                "(select count(a) from AiStockInnovationScore a where a.totalScore between 70 and 80) as score80, " +
                "(select count(a) from AiStockInnovationScore a where a.totalScore between 80 and 90) as score90, " +
                "(select count(a) from AiStockInnovationScore a where a.totalScore between 90 and 100) as score100, " +
                "count(n) as total " +
            "from AiStockInnovationScore n")
    ResponseAiMapper.AiInnovationChartInterface getInnovationAllChart();

    /**
     * 코스피 최소/평균/최대 값 조회
     * @return
     */
    @Query("select " +
                "ifnull(min(a.totalScore), 0) as min, " +
                "ifnull(avg(a.totalScore), 0) as avg, " +
                "ifnull(max(a.totalScore), 0) as max " +
            "from AiStockInnovationScore a " +
            "inner join JStockJong b on a.stockCode = b.iscd " +
            "where b.mrktDivClsCode = '1'")
    ResponseAiMapper.AiInnovationAggregationInterface getAggregationKospi();
    /**
     * 코스피 분포도 조회(구분 단위 10점 ~ 최대 100점)
     * @return
     */
    @Query("select " +
                "(select count(distinct a.stockCode) from AiStockInnovationScore a " +
                "inner join JStockJong b on a.stockCode = b.iscd " +
                "where b.mrktDivClsCode = '1' " +
                "and a.totalScore between 0 and 10) as score10, " +
                "(select count(distinct a.stockCode) from AiStockInnovationScore a " +
                "inner join JStockJong b on a.stockCode = b.iscd " +
                "where b.mrktDivClsCode = '1' " +
                "and a.totalScore between 10 and 20) as score20, " +
                "(select count(distinct a.stockCode) from AiStockInnovationScore a " +
                "inner join JStockJong b on a.stockCode = b.iscd " +
                "where b.mrktDivClsCode = '1' " +
                "and a.totalScore between 20 and 30) as score30, " +
                "(select count(distinct a.stockCode) from AiStockInnovationScore a " +
                "inner join JStockJong b on a.stockCode = b.iscd " +
                "where b.mrktDivClsCode = '1' " +
                "and a.totalScore between 30 and 40) as score40, " +
                "(select count(distinct a.stockCode) from AiStockInnovationScore a " +
                "inner join JStockJong b on a.stockCode = b.iscd " +
                "where b.mrktDivClsCode = '1' " +
                "and a.totalScore between 40 and 50) as score50, " +
                "(select count(distinct a.stockCode) from AiStockInnovationScore a " +
                "inner join JStockJong b on a.stockCode = b.iscd " +
                "where b.mrktDivClsCode = '1' " +
                "and a.totalScore between 50 and 60) as score60, " +
                "(select count(distinct a.stockCode) from AiStockInnovationScore a " +
                "inner join JStockJong b on a.stockCode = b.iscd " +
                "where b.mrktDivClsCode = '1' " +
                "and a.totalScore between 60 and 70) as score70, " +
                "(select count(distinct a.stockCode) from AiStockInnovationScore a " +
                "inner join JStockJong b on a.stockCode = b.iscd " +
                "where b.mrktDivClsCode = '1' " +
                "and a.totalScore between 70 and 80) as score80, " +
                "(select count(distinct a.stockCode) from AiStockInnovationScore a " +
                "inner join JStockJong b on a.stockCode = b.iscd " +
                "where b.mrktDivClsCode = '1' " +
                "and a.totalScore between 80 and 90) as score90, " +
                "(select count(distinct a.stockCode) from AiStockInnovationScore a " +
                "inner join JStockJong b on a.stockCode = b.iscd " +
                "where b.mrktDivClsCode = '1' " +
                "and a.totalScore between 90 and 100) as score100, " +
                "count(distinct a.stockCode) as total " +
            "from AiStockInnovationScore a " +
            "inner join JStockJong b on a.stockCode = b.iscd " +
            "where b.mrktDivClsCode = '1'")
    ResponseAiMapper.AiInnovationChartInterface getInnovationKospiChart();

    /**
     * 코스닥 최소/평균/최대 값 조회
     * @return
     */
    @Query("select " +
                "ifnull(min(a.totalScore), 0) as min, " +
                "ifnull(avg(a.totalScore), 0) as avg, " +
                "ifnull(max(a.totalScore), 0) as max " +
            "from AiStockInnovationScore a " +
            "inner join JStockJong b on a.stockCode = b.iscd " +
            "where b.mrktDivClsCode = '2'")
    ResponseAiMapper.AiInnovationAggregationInterface getAggregationKosdaq();
    /**
     * 코스닥 분포도 조회(구분 단위 10점 ~ 최대 100점)
     * @return
     */
    @Query("select " +
                "(select count(distinct a.stockCode) from AiStockInnovationScore a " +
                "inner join JStockJong b on a.stockCode = b.iscd " +
                "where b.mrktDivClsCode = '2' " +
                "and a.totalScore between 0 and 10) as score10, " +
                "(select count(distinct a.stockCode) from AiStockInnovationScore a " +
                "inner join JStockJong b on a.stockCode = b.iscd " +
                "where b.mrktDivClsCode = '2' " +
                "and a.totalScore between 10 and 20) as score20, " +
                "(select count(distinct a.stockCode) from AiStockInnovationScore a " +
                "inner join JStockJong b on a.stockCode = b.iscd " +
                "where b.mrktDivClsCode = '2' " +
                "and a.totalScore between 20 and 30) as score30, " +
                "(select count(distinct a.stockCode) from AiStockInnovationScore a " +
                "inner join JStockJong b on a.stockCode = b.iscd " +
                "where b.mrktDivClsCode = '2' " +
                "and a.totalScore between 30 and 40) as score40, " +
                "(select count(distinct a.stockCode) from AiStockInnovationScore a " +
                "inner join JStockJong b on a.stockCode = b.iscd " +
                "where b.mrktDivClsCode = '2' " +
                "and a.totalScore between 40 and 50) as score50, " +
                "(select count(distinct a.stockCode) from AiStockInnovationScore a " +
                "inner join JStockJong b on a.stockCode = b.iscd " +
                "where b.mrktDivClsCode = '2' " +
                "and a.totalScore between 50 and 60) as score60, " +
                "(select count(distinct a.stockCode) from AiStockInnovationScore a " +
                "inner join JStockJong b on a.stockCode = b.iscd " +
                "where b.mrktDivClsCode = '2' " +
                "and a.totalScore between 60 and 70) as score70, " +
                "(select count(distinct a.stockCode) from AiStockInnovationScore a " +
                "inner join JStockJong b on a.stockCode = b.iscd " +
                "where b.mrktDivClsCode = '2' " +
                "and a.totalScore between 70 and 80) as score80, " +
                "(select count(distinct a.stockCode) from AiStockInnovationScore a " +
                "inner join JStockJong b on a.stockCode = b.iscd " +
                "where b.mrktDivClsCode = '2' " +
                "and a.totalScore between 80 and 90) as score90, " +
                "(select count(distinct a.stockCode) from AiStockInnovationScore a " +
                "inner join JStockJong b on a.stockCode = b.iscd " +
                "where b.mrktDivClsCode = '2' " +
                "and a.totalScore between 90 and 100) as score100, " +
                "count(distinct a.stockCode) as total " +
            "from AiStockInnovationScore a " +
            "inner join JStockJong b on a.stockCode = b.iscd " +
            "where b.mrktDivClsCode = '2'")
    ResponseAiMapper.AiInnovationChartInterface getInnovationKosdaqChart();

    /**
     * 동일업종 분포도 조회(구분 단위 10점 ~ 최대 100점)
     * @return
     */
    @Query("select " +
                "ifnull(min(a.totalScore), 0) as min, " +
                "ifnull(avg(a.totalScore), 0) as avg, " +
                "ifnull(max(a.totalScore), 0) as max " +
            "from AiStockInnovationScore a " +
            "inner join JStockJong b on a.stockCode = b.iscd " +
            "where b.bstpLargDivCode = :largeCode " +
            "and b.bstpMedmDivCode = :mediumCode " +
            "and b.bstpSmalDivCode = :smallCode")
    ResponseAiMapper.AiInnovationAggregationInterface getAggregationIndustry(String largeCode, String mediumCode, String smallCode);
    /**
     * 동일업종 분포도 조회(구분 단위 10점 ~ 최대 100점)
     * @return
     */
    @Query("select " +
                "(select count(distinct a.stockCode) from AiStockInnovationScore a " +
                "inner join JStockJong b on a.stockCode = b.iscd " +
                "where b.bstpLargDivCode = :largeCode " +
                "and b.bstpMedmDivCode = :mediumCode " +
                "and b.bstpSmalDivCode = :smallCode " +
                "and a.totalScore between 0 and 10) as score10, " +
                "(select count(distinct a.stockCode) from AiStockInnovationScore a " +
                "inner join JStockJong b on a.stockCode = b.iscd " +
                "where b.bstpLargDivCode = :largeCode " +
                "and b.bstpMedmDivCode = :mediumCode " +
                "and b.bstpSmalDivCode = :smallCode " +
                "and a.totalScore between 10 and 20) as score20, " +
                "(select count(distinct a.stockCode) from AiStockInnovationScore a " +
                "inner join JStockJong b on a.stockCode = b.iscd " +
                "where b.bstpLargDivCode = :largeCode " +
                "and b.bstpMedmDivCode = :mediumCode " +
                "and b.bstpSmalDivCode = :smallCode " +
                "and a.totalScore between 20 and 30) as score30, " +
                "(select count(distinct a.stockCode) from AiStockInnovationScore a " +
                "inner join JStockJong b on a.stockCode = b.iscd " +
                "where b.bstpLargDivCode = :largeCode " +
                "and b.bstpMedmDivCode = :mediumCode " +
                "and b.bstpSmalDivCode = :smallCode " +
                "and a.totalScore between 30 and 40) as score40, " +
                "(select count(distinct a.stockCode) from AiStockInnovationScore a " +
                "inner join JStockJong b on a.stockCode = b.iscd " +
                "where b.bstpLargDivCode = :largeCode " +
                "and b.bstpMedmDivCode = :mediumCode " +
                "and b.bstpSmalDivCode = :smallCode " +
                "and a.totalScore between 40 and 50) as score50, " +
                "(select count(distinct a.stockCode) from AiStockInnovationScore a " +
                "inner join JStockJong b on a.stockCode = b.iscd " +
                "where b.bstpLargDivCode = :largeCode " +
                "and b.bstpMedmDivCode = :mediumCode " +
                "and b.bstpSmalDivCode = :smallCode " +
                "and a.totalScore between 50 and 60) as score60, " +
                "(select count(distinct a.stockCode) from AiStockInnovationScore a " +
                "inner join JStockJong b on a.stockCode = b.iscd " +
                "where b.bstpLargDivCode = :largeCode " +
                "and b.bstpMedmDivCode = :mediumCode " +
                "and b.bstpSmalDivCode = :smallCode " +
                "and a.totalScore between 60 and 70) as score70, " +
                "(select count(distinct a.stockCode) from AiStockInnovationScore a " +
                "inner join JStockJong b on a.stockCode = b.iscd " +
                "where b.bstpLargDivCode = :largeCode " +
                "and b.bstpMedmDivCode = :mediumCode " +
                "and b.bstpSmalDivCode = :smallCode " +
                "and a.totalScore between 70 and 80) as score80, " +
                "(select count(distinct a.stockCode) from AiStockInnovationScore a " +
                "inner join JStockJong b on a.stockCode = b.iscd " +
                "where b.bstpLargDivCode = :largeCode " +
                "and b.bstpMedmDivCode = :mediumCode " +
                "and b.bstpSmalDivCode = :smallCode " +
                "and a.totalScore between 80 and 90) as score90, " +
                "(select count(distinct a.stockCode) from AiStockInnovationScore a " +
                "inner join JStockJong b on a.stockCode = b.iscd " +
                "where b.bstpLargDivCode = :largeCode " +
                "and b.bstpMedmDivCode = :mediumCode " +
                "and b.bstpSmalDivCode = :smallCode " +
                "and a.totalScore between 90 and 100) as score100, " +
                "count(distinct a.stockCode) as total " +
            "from AiStockInnovationScore a " +
            "inner join JStockJong b on a.stockCode = b.iscd " +
            "where b.bstpLargDivCode = :largeCode " +
            "and b.bstpMedmDivCode = :mediumCode " +
            "and b.bstpSmalDivCode = :smallCode")
    ResponseAiMapper.AiInnovationChartInterface getInnovationIndustryChart(String largeCode, String mediumCode, String smallCode);
    /**
     * 동일테마 최소/평균/최대 값 조회
     * @return
     */
    @Query("select " +
                "ifnull(min(a.totalScore), 0) as min, " +
                "ifnull(avg(a.totalScore), 0) as avg, " +
                "ifnull(max(a.totalScore), 0) as max " +
            "from AiStockInnovationScore a " +
            "where a.stockCode in (:stockCodeList)")
    ResponseAiMapper.AiInnovationAggregationInterface getAggregationTheme(List<String> stockCodeList);
    /**
     * 동일테마 분포도 조회(구분 단위 10점 ~ 최대 100점)
     * @return
     */
    @Query("select " +
                "(select count(distinct a.stockCode) from AiStockInnovationScore a " +
                "where a.stockCode in (:stockCodeList) " +
                "and a.totalScore between 0 and 10) as score10, " +
                "(select count(distinct a.stockCode) from AiStockInnovationScore a " +
                "where a.stockCode in (:stockCodeList) " +
                "and a.totalScore between 10 and 20) as score20, " +
                "(select count(distinct a.stockCode) from AiStockInnovationScore a " +
                "where a.stockCode in (:stockCodeList) " +
                "and a.totalScore between 20 and 30) as score30, " +
                "(select count(distinct a.stockCode) from AiStockInnovationScore a " +
                "where a.stockCode in (:stockCodeList) " +
                "and a.totalScore between 30 and 40) as score40, " +
                "(select count(distinct a.stockCode) from AiStockInnovationScore a " +
                "where a.stockCode in (:stockCodeList) " +
                "and a.totalScore between 40 and 50) as score50, " +
                "(select count(distinct a.stockCode) from AiStockInnovationScore a " +
                "where a.stockCode in (:stockCodeList) " +
                "and a.totalScore between 50 and 60) as score60, " +
                "(select count(distinct a.stockCode) from AiStockInnovationScore a " +
                "where a.stockCode in (:stockCodeList) " +
                "and a.totalScore between 60 and 70) as score70, " +
                "(select count(distinct a.stockCode) from AiStockInnovationScore a " +
                "where a.stockCode in (:stockCodeList) " +
                "and a.totalScore between 70 and 80) as score80, " +
                "(select count(distinct a.stockCode) from AiStockInnovationScore a " +
                "where a.stockCode in (:stockCodeList) " +
                "and a.totalScore between 80 and 90) as score90, " +
                "(select count(distinct a.stockCode) from AiStockInnovationScore a " +
                "where a.stockCode in (:stockCodeList) " +
                "and a.totalScore between 90 and 100) as score100, " +
                "count(distinct a.stockCode) as total " +
            "from AiStockInnovationScore a " +
            "where a.stockCode in (:stockCodeList)")
    ResponseAiMapper.AiInnovationChartInterface getInnovationThemeChart(List<String> stockCodeList);
}