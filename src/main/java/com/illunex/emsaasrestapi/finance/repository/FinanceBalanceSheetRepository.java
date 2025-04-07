package com.illunex.emsaasrestapi.finance.repository;

import com.illunex.emsaasrestapi.finance.entity.FinanceBalanceSheet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FinanceBalanceSheetRepository extends JpaRepository<FinanceBalanceSheet, Long> {
    Optional<FinanceBalanceSheet> findByIscdAndConsolidatedAndFiscalYearEnd(String iscd, String consolidated, int fiscalYearEnd);
}