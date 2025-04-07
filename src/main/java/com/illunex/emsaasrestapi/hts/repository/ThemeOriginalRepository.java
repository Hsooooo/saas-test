package com.illunex.emsaasrestapi.hts.repository;

import com.illunex.emsaasrestapi.hts.dto.ResponseHtsMapper;
import com.illunex.emsaasrestapi.hts.entity.ThemeOriginal;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ThemeOriginalRepository extends JpaRepository<ThemeOriginal, Long> {
    List<ThemeOriginal> findAllByIscd(String stockCode);

    List<ThemeOriginal> findAllByThemeIdxIn(List<Long> thgemeIdxList);

    @Query(value = "select theme_idx as themeIdx, sum(prdy_ctrt)/count(*)as prdyCtrt from theme_original\n" +
            "where scrap_date is not null\n " +
            "group by theme_idx", nativeQuery = true)
    List<ResponseHtsMapper.ThemeOriginalGroupInterface> findThemeGroup();

    @Query(value = "select iscd, sum(prdy_ctrt)/count(*)as prdyCtrt from theme_original\n" +
            "where iscd = :iscd \n " +
            "group by iscd", nativeQuery = true)
    ResponseHtsMapper.ThemeOriginalIscdPrdyCtrtGroupInterface findByKsicIscd(String iscd);

    List<ThemeOriginal> findAllByThemeIdx(Long themeIdx, Pageable pageable);
    Long countAllByThemeIdx(Long themeIdx);
}