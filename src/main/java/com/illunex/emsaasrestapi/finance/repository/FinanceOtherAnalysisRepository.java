package com.illunex.emsaasrestapi.finance.repository;

import com.illunex.emsaasrestapi.finance.entity.FinanceOtherAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FinanceOtherAnalysisRepository extends JpaRepository<FinanceOtherAnalysis, Long> {
    Optional<FinanceOtherAnalysis> findByIscdAndConsolidatedAndFiscalYearEnd(String iscd, String consolidated, int fiscalYearEnd);

    @Query(value = "SELECT idx, iscd, financial_reporting_period, LEFT(fiscal_year_end, 4) AS fiscal_year_end, consolidated, SUM(revenue) AS revenue, SUM(gross_profit) AS gross_profit, SUM(operating_income) AS operating_income, SUM(pretax_income) AS pretax_income, SUM(net_income) AS net_income, create_date" +
                    " FROM finance_other_analysis WHERE iscd = :iscd and consolidated = :consolidated" +
                    " GROUP BY LEFT(fiscal_year_end, 4)" +
                    " ORDER BY fiscal_year_end DESC" +
                    " LIMIT :topCnt", nativeQuery = true
    )
    List<FinanceOtherAnalysis> findAnnualTopByIscdAndConsolidatedOrderByFiscalYearEndDesc(int topCnt, String iscd, String consolidated);

    @Query(value = "SELECT idx, iscd, financial_reporting_period, fiscal_year_end AS fiscal_year_end, consolidated, revenue, gross_profit, operating_income, pretax_income, net_income, create_date" +
                    " FROM finance_other_analysis WHERE iscd = :iscd and consolidated = :consolidated" +
                    " ORDER BY fiscal_year_end DESC" +
                    " LIMIT :topCnt", nativeQuery = true
    )
    List<FinanceOtherAnalysis> findQuarterTopByIscdAndConsolidatedOrderByFiscalYearEndDesc(int topCnt, String iscd, String consolidated);
}