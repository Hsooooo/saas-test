package com.illunex.emsaasrestapi.ai.repository;

import com.illunex.emsaasrestapi.ai.entity.AiStockValues;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AiStockValuesRepository extends JpaRepository<AiStockValues, Long> {
    List<AiStockValues> findAllByStockCodeAndStockDateGreaterThanEqualOrderByStockDateDesc(String stockCode, LocalDate startDate);
    @Query("select s from AiStockValues s " +
            "inner join AiFinanceIndexes f " +
            "on s.stockDate = f.stockDate " +
            "and s.stockCode = :stockCode " +
            "and s.stockDate >= :startDate")
    List<AiStockValues> getNotEmptyStockDateFinanceIndexes(String stockCode, LocalDate startDate);
}