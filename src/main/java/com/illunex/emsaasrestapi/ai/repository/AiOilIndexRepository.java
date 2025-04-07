package com.illunex.emsaasrestapi.ai.repository;

import com.illunex.emsaasrestapi.ai.entity.AiOilIndex;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AiOilIndexRepository extends JpaRepository<AiOilIndex, Long> {
    List<AiOilIndex> findAllByStockDateInOrderByStockDateDesc(List<LocalDate> stockDate);
}