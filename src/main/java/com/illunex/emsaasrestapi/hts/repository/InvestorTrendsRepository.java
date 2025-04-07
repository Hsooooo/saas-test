package com.illunex.emsaasrestapi.hts.repository;

import com.illunex.emsaasrestapi.hts.entity.InvestorTrends;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvestorTrendsRepository extends JpaRepository<InvestorTrends, Long> {
    InvestorTrends findTop1ByIscdOrderByInvestorDateDesc(String iscd);
}