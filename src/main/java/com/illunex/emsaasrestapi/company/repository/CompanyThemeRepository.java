package com.illunex.emsaasrestapi.company.repository;

import com.illunex.emsaasrestapi.company.entity.CompanyTheme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompanyThemeRepository extends JpaRepository<CompanyTheme, Long> {
    List<CompanyTheme> findByIscdOrderByThemeIdx(String iscd);
}