package com.illunex.emsaasrestapi.company.repository;

import com.illunex.emsaasrestapi.company.entity.CompanyDesc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyDescRepository extends JpaRepository<CompanyDesc, Integer> {
    Optional<CompanyDesc> findByIscd(String stockCode);
}