package com.illunex.emsaasrestapi.hts.repository;

import com.illunex.emsaasrestapi.hts.entity.ThemeCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ThemeCodeRepository extends JpaRepository<ThemeCode, Long> {
    List<ThemeCode> findByIdxIn(List<Long> themeCodeIdxList);
    ThemeCode findByIdx(Long themeIdx);
    List<ThemeCode> findAllByScrapDateIsNotNull();
}