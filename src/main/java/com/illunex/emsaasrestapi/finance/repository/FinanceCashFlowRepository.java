package com.illunex.emsaasrestapi.finance.repository;

import com.illunex.emsaasrestapi.finance.entity.FinanceCashFlow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FinanceCashFlowRepository extends JpaRepository<FinanceCashFlow, Long> {
}