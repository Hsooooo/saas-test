package com.illunex.emsaasrestapi.ai.repository;

import com.illunex.emsaasrestapi.ai.entity.AiFinanceIndexes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AiFinanceIndexesRepository extends JpaRepository<AiFinanceIndexes, Long> {
    List<AiFinanceIndexes> findAllByStockDateInOrderByStockDateDesc(List<LocalDate> stockDate);
}