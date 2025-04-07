package com.illunex.emsaasrestapi.hts.repository;

import com.illunex.emsaasrestapi.hts.entity.ThemeLogos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ThemeLogosRepository extends JpaRepository<ThemeLogos, Long> {
    ThemeLogos findByThemeIdx(Integer themeIdx);
}