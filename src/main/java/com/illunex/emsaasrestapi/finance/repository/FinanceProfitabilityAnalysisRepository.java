package com.illunex.emsaasrestapi.finance.repository;

import com.illunex.emsaasrestapi.finance.entity.FinanceProfitabilityAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FinanceProfitabilityAnalysisRepository extends JpaRepository<FinanceProfitabilityAnalysis, Long> {
    Optional<FinanceProfitabilityAnalysis> findByIscdAndConsolidatedAndFiscalYearEnd(String iscd, String consolidated, int fiscalYearEnd);

    FinanceProfitabilityAnalysis findTop1ByIscdAndConsolidatedOrderByFiscalYearEndDesc(String iscd, String consolidated);
}