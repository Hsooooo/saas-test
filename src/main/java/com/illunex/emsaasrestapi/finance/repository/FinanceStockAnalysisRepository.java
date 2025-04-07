package com.illunex.emsaasrestapi.finance.repository;

import com.illunex.emsaasrestapi.finance.entity.FinanceStockAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FinanceStockAnalysisRepository extends JpaRepository<FinanceStockAnalysis, Long> {
    Optional<FinanceStockAnalysis> findTop1ByIscdAndConsolidatedOrderByFiscalYearEndDesc(String iscd, String consolidated);

    @Query(value = "SELECT * FROM finance_stock_analysis WHERE iscd = :iscd AND consolidated = :consolidated ORDER BY fiscal_year_end DESC LIMIT :cnt"
            , nativeQuery = true)
    List<FinanceStockAnalysis> findQuarterTopByIscdAndConsolidatedOrderByFiscalYearEndDesc(int cnt, String iscd, String consolidated);

    @Query(value = "SELECT idx, financial_reporting_period, consolidated, LEFT(fiscal_year_end, 4) AS fiscal_year_end, iscd," +
            " per, pbr, eps, ev_ebitda, psr, pcr, peg, dividend_yield, bps, sps, cfps, market_cap, share_capital, eva, create_date" +
            " FROM finance_stock_analysis" +
            " WHERE iscd = :iscd AND consolidated = :consolidated " +
            " GROUP BY LEFT(fiscal_year_end, 4) " +
            " ORDER BY fiscal_year_end DESC " +
            "LIMIT :cnt", nativeQuery = true)
    List<FinanceStockAnalysis> findAnnualTopByIscdAndConsolidatedOrderByFiscalYearEndDesc(int cnt, String iscd, String consolidated);
}