package com.illunex.emsaasrestapi.company.repository;

import com.illunex.emsaasrestapi.company.entity.CompanyInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompanyInfoRepository extends JpaRepository<CompanyInfo, Long> {
    CompanyInfo findByIscd(String iscd);
    CompanyInfo findTop1ByOrderBySyncDateDesc();

    @Query(value = "select * from company_info where date(sync_date) = :syncDate and business_category_code_11 is not null", nativeQuery = true)
    List<CompanyInfo> findBySyncDate(String syncDate);
}