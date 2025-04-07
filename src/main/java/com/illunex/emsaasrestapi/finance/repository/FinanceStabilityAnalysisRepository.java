package com.illunex.emsaasrestapi.finance.repository;

import com.illunex.emsaasrestapi.finance.entity.FinanceStabilityAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FinanceStabilityAnalysisRepository extends JpaRepository<FinanceStabilityAnalysis, Long> {
    @Query(value = "SELECT idx, iscd, financial_reporting_period, LEFT(fiscal_year_end, 4) AS fiscal_year_end, consolidated, debt_ratio," +
            "financial_cost_burden_rate, short_term_borrowing_ratio, debt_dependency, accounts_receivable_to_revenue_ratio," +
            "inventory_to_revenue_ratio, interest_coverage_ratio, net_debt, short_term_debt, operating_income_to_interest_ratio, create_date" +
            " FROM finance_stability_analysis WHERE iscd = :iscd and consolidated = :consolidated" +
            " GROUP BY LEFT(fiscal_year_end, 4)" +
            " ORDER BY fiscal_year_end DESC" +
            " LIMIT :topCnt", nativeQuery = true)
    List<FinanceStabilityAnalysis> findAnnualTopByIscdAndConsolidatedOrderByFiscalYearEndDesc(int topCnt, String iscd, String consolidated);

    @Query(value = "SELECT idx, iscd, financial_reporting_period, fiscal_year_end, consolidated, debt_ratio," +
            "financial_cost_burden_rate, short_term_borrowing_ratio, debt_dependency, accounts_receivable_to_revenue_ratio," +
            "inventory_to_revenue_ratio, interest_coverage_ratio, net_debt, short_term_debt, operating_income_to_interest_ratio, create_date" +
            " FROM finance_stability_analysis WHERE iscd = :iscd and consolidated = :consolidated" +
            " ORDER BY fiscal_year_end DESC" +
            " LIMIT :topCnt", nativeQuery = true)
    List<FinanceStabilityAnalysis> findQuarterTopByIscdAndConsolidatedOrderByFiscalYearEndDesc(int topCnt, String iscd, String consolidated);
}