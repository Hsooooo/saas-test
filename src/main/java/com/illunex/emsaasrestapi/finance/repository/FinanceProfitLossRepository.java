package com.illunex.emsaasrestapi.finance.repository;

import com.illunex.emsaasrestapi.finance.entity.FinanceProfitLoss;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FinanceProfitLossRepository extends JpaRepository<FinanceProfitLoss, Long> {
}