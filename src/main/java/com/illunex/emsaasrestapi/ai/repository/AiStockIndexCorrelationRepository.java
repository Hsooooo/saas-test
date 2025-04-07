package com.illunex.emsaasrestapi.ai.repository;

import com.illunex.emsaasrestapi.ai.entity.AiStockIndexCorrelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AiStockIndexCorrelationRepository extends JpaRepository<AiStockIndexCorrelation, Long> {
    Optional<AiStockIndexCorrelation> findTop1ByStockCodeAndCalcRangeOrderByStockDateDesc(String stockCode, Integer calcRange);
}