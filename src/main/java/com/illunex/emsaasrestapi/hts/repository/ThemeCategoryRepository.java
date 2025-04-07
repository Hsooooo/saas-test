package com.illunex.emsaasrestapi.hts.repository;

import com.illunex.emsaasrestapi.hts.entity.ThemeCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ThemeCategoryRepository extends JpaRepository<ThemeCategory, Long> {
}