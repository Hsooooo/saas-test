package com.illunex.emsaasrestapi.hts.repository;

import com.illunex.emsaasrestapi.hts.entity.JSise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JsiseRepository extends JpaRepository<JSise, Long> {
    Optional<JSise> findTop1ByIscdOrderByBsopDateDescBsopHourDesc(String iscd);
    JSise findTop1ByIscdAndBsopDate(String stockCode, int bsopDate);

    @Query(value = "select * from jsise\n" +
            " where iscd = :iscd \n" +
            "    and STR_TO_DATE(bsop_date, '%Y%m%d')\n" +
            "    >= DATE_SUB(:bsopDate, INTERVAL 7 DAY)\n" +
            " order by bsop_date\n" +
            " limit 1", nativeQuery = true)
    JSise findTop1ByWeekPrdyCtrt(String iscd, Integer bsopDate);
    @Query(value = "select * from jsise\n" +
            " where iscd = :iscd \n" +
            "    and STR_TO_DATE(bsop_date, '%Y%m%d')\n" +
            "    >= DATE_SUB(:bsopDate, INTERVAL 1 MONTH)\n" +
            " order by bsop_date\n" +
            " limit 1", nativeQuery = true)
    JSise findTop1ByMonthPrdyCtrt(String iscd, Integer bsopDate);
    @Query(value = "select * from jsise\n" +
            " where iscd = :iscd \n" +
            "    and STR_TO_DATE(bsop_date, '%Y%m%d')\n" +
            "    >= DATE_SUB(:bsopDate, INTERVAL 1 YEAR)\n" +
            " order by bsop_date\n" +
            " limit 1", nativeQuery = true)
    JSise findTop1ByYearPrdyCtrt(String iscd, Integer bsopDate);

    @Query(value = "select * from jsise\n" +
            " where iscd = :iscd \n" +
            "    and STR_TO_DATE(bsop_date, '%Y%m%d')\n" +
            "    >= DATE_SUB(:bsopDate, INTERVAL 1 DAY)\n" +
            " order by bsop_date\n" +
            " limit 1", nativeQuery = true)
    JSise findTop1ByDayPrdyCtrt(String iscd, Integer bsopDate);
}