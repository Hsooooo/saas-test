package com.illunex.emsaasrestapi.ai.repository;

import com.illunex.emsaasrestapi.ai.entity.AiDartOrderRcept;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiDartOrderRceptRepository extends JpaRepository<AiDartOrderRcept, Long> {
    List<AiDartOrderRcept> findAllByStockCode(String stockCode, Pageable pageable);
    Long countAllByStockCode(String stockCode);
}