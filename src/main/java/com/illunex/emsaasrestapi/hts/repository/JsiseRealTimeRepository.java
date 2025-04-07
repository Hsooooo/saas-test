package com.illunex.emsaasrestapi.hts.repository;

import com.illunex.emsaasrestapi.hts.dto.ResponseHtsMapper;
import com.illunex.emsaasrestapi.hts.entity.JsiseRealTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JsiseRealTimeRepository extends JpaRepository<JsiseRealTime, Long> {
    @Query(
            value = "SELECT a.iscd, a.bsop_date as bsopDate, a.bsop_hour as bsopHour, a.kor_isnm as korIsnm, a.prpr, a.prdy_ctrt as prdyCtrt, " +
                    "a.prdy_vrss as prdyVrss, a.prdy_vrss_sign as prdyVrssSign, a.w52_hgpr as w52Hgpr, a.w52_hgpr_date as w52HgprDate, " +
                    "a.w52_lwpr as w52Lwpr, a.w52_lwpr_date as w52LwprDate, a.acml_vol AS acmlVol, a.acml_tr_pbmn as acmlTrPbmn, " +
                    "a.oprc, a.hgpr, a.lwpr, a.mxpr, a.llam, a.avls, a.prdy_vol as prdyVol, a.trht_yn as trhtYn, " +
                    "a.create_date as createDate, a.update_date as updateDate, " +
                    "b.fiscal_year_end as fiscalYearEnd, b.per, b.pbr " +
                    "FROM jsise_real_time a " +
                    "LEFT JOIN ( " +
                    "    SELECT iscd, fiscal_year_end, pbr, per " +
                    "    FROM finance_stock_analysis " +
                    "    WHERE (iscd, fiscal_year_end) IN ( " +
                    "        SELECT iscd, MAX(fiscal_year_end) AS latest_fiscal_year_end " +
                    "        FROM finance_stock_analysis " +
                    "        WHERE consolidated = :consolidated " +
                    "        GROUP BY iscd " +
                    "    ) " +
                    "    AND consolidated = :consolidated " +
                    ") AS b ON a.iscd = b.iscd ",
            countQuery = "SELECT COUNT(1) " +
                    "FROM jsise_real_time a " +
                    "LEFT JOIN ( " +
                    "    SELECT iscd, fiscal_year_end, pbr, per " +
                    "    FROM finance_stock_analysis " +
                    "    WHERE (iscd, fiscal_year_end) IN ( " +
                    "        SELECT iscd, MAX(fiscal_year_end) AS latest_fiscal_year_end " +
                    "        FROM finance_stock_analysis " +
                    "        WHERE consolidated = :consolidated " +
                    "        GROUP BY iscd " +
                    "    ) " +
                    "    AND consolidated = :consolidated " +
                    ") AS b ON a.iscd = b.iscd ",
            nativeQuery = true)
    Page<ResponseHtsMapper.JsiseRealTimeInerface> findJsiseRealTImeJoinFinanceStockAnalysis(PageRequest pageable, String consolidated);

    @Query(value = "SELECT a.iscd, a.bsop_date as bsopDate, a.bsop_hour as bsopHour, a.kor_isnm as korIsnm, a.prpr, a.prdy_ctrt as prdyCtrt, " +
            "a.prdy_vrss as prdyVrss, a.prdy_vrss_sign as prdyVrssSign, a.w52_hgpr as w52Hgpr, a.w52_hgpr_date as w52HgprDate, " +
            "a.w52_lwpr as w52Lwpr, a.w52_lwpr_date as w52LwprDate, a.acml_vol AS acmlVol, a.acml_tr_pbmn as acmlTrPbmn, " +
            "a.oprc, a.hgpr, a.lwpr, a.mxpr, a.llam, a.avls, a.prdy_vol as prdyVol, a.trht_yn as trhtYn, " +
            "a.create_date as createDate, a.update_date as updateDate, " +
            "b.fiscal_year_end as fiscalYearEnd, b.per, b.pbr " +
            "FROM jsise_real_time a " +
            "LEFT JOIN ( " +
            "    SELECT iscd, fiscal_year_end, pbr, per " +
            "    FROM finance_stock_analysis " +
            "    WHERE (iscd, fiscal_year_end) IN ( " +
            "        SELECT iscd, MAX(fiscal_year_end) AS latest_fiscal_year_end " +
            "        FROM finance_stock_analysis " +
            "        WHERE consolidated = :consolidated " +
            "        AND iscd in (:searchIscds)" +
            "        GROUP BY iscd " +
            "    ) " +
            "    AND consolidated = :consolidated " +
            "    AND iscd in (:searchIscds)" +
            ") AS b ON a.iscd = b.iscd " +
            "WHERE a.iscd in (:searchIscds) " +
            "ORDER BY a.iscd ASC",
            nativeQuery = true)
    List<ResponseHtsMapper.JsiseRealTimeInerface> findAllJsiseRealTimeAndFinanceStockAnalysisByIscdIn(List<String> searchIscds, String consolidated);
    @Query(value = "SELECT a.iscd, a.bsop_date as bsopDate, a.bsop_hour as bsopHour, a.kor_isnm as korIsnm, a.prpr, a.prdy_ctrt as prdyCtrt, " +
            "a.prdy_vrss as prdyVrss, a.prdy_vrss_sign as prdyVrssSign, a.w52_hgpr as w52Hgpr, a.w52_hgpr_date as w52HgprDate, " +
            "a.w52_lwpr as w52Lwpr, a.w52_lwpr_date as w52LwprDate, a.acml_vol AS acmlVol, a.acml_tr_pbmn as acmlTrPbmn, " +
            "a.oprc, a.hgpr, a.lwpr, a.mxpr, a.llam, a.avls, a.prdy_vol as prdyVol, a.trht_yn as trhtYn, " +
            "a.create_date as createDate, a.update_date as updateDate, " +
            "b.fiscal_year_end as fiscalYearEnd, b.per, b.pbr " +
            "FROM jsise_real_time a " +
            "LEFT JOIN ( " +
            "    SELECT iscd, fiscal_year_end, pbr, per " +
            "    FROM finance_stock_analysis " +
            "    WHERE (iscd, fiscal_year_end) IN ( " +
            "        SELECT iscd, MAX(fiscal_year_end) AS latest_fiscal_year_end " +
            "        FROM finance_stock_analysis " +
            "        WHERE consolidated = :consolidated " +
            "        AND iscd in (:searchIscds)" +
            "        GROUP BY iscd " +
            "    ) " +
            "    AND consolidated = :consolidated AND iscd in (:searchIscds)" +
            ") AS b ON a.iscd = b.iscd " +
            " WHERE a.iscd in (:searchIscds)",
            countQuery = "SELECT COUNT(1) " +
                    "FROM jsise_real_time a " +
                    "LEFT JOIN ( " +
                    "    SELECT iscd, fiscal_year_end, pbr, per " +
                    "    FROM finance_stock_analysis " +
                    "    WHERE (iscd, fiscal_year_end) IN ( " +
                    "        SELECT iscd, MAX(fiscal_year_end) AS latest_fiscal_year_end " +
                    "        FROM finance_stock_analysis " +
                    "        WHERE consolidated = :consolidated " +
                    "        AND iscd in (:searchIscds)" +
                    "        GROUP BY iscd " +
                    "    ) " +
                    "    AND consolidated = :consolidated AND iscd in (:searchIscds)" +
                    ") AS b ON a.iscd = b.iscd " +
                    " WHERE a.iscd in (:searchIscds)",
            nativeQuery = true)
    Page<ResponseHtsMapper.JsiseRealTimeInerface> findByJsiseRealTimeIscdInAndfinanceStockAnalysis(List<String> searchIscds, String consolidated, Pageable pageable);
    @Query(value = "SELECT a.iscd, a.bsop_date as bsopDate, a.bsop_hour as bsopHour, a.kor_isnm as korIsnm, a.prpr, a.prdy_ctrt as prdyCtrt, " +
            "a.prdy_vrss as prdyVrss, a.prdy_vrss_sign as prdyVrssSign, a.w52_hgpr as w52Hgpr, a.w52_hgpr_date as w52HgprDate, " +
            "a.w52_lwpr as w52Lwpr, a.w52_lwpr_date as w52LwprDate, a.acml_vol AS acmlVol, a.acml_tr_pbmn as acmlTrPbmn, " +
            "a.oprc, a.hgpr, a.lwpr, a.mxpr, a.llam, a.avls, a.prdy_vol as prdyVol, a.trht_yn as trhtYn, " +
            "a.create_date as createDate, a.update_date as updateDate, " +
            "b.fiscal_year_end as fiscalYearEnd, b.per, b.pbr " +
            "FROM jsise_real_time a " +
            "LEFT JOIN ( " +
            "    SELECT iscd, fiscal_year_end, pbr, per " +
            "    FROM finance_stock_analysis " +
            "    WHERE (iscd, fiscal_year_end) IN ( " +
            "        SELECT iscd, MAX(fiscal_year_end) AS latest_fiscal_year_end " +
            "        FROM finance_stock_analysis " +
            "        WHERE consolidated = :consolidated " +
            "        GROUP BY iscd " +
            "    ) " +
            "    AND consolidated = :consolidated " +
            ") AS b ON a.iscd = b.iscd " +
            "ORDER BY a.iscd ASC",
            nativeQuery = true)
    List<ResponseHtsMapper.JsiseRealTimeInerface> findAllJsiseRealTimeAndFinanceStockAnalysis(String consolidated);
    JsiseRealTime findByIscd(String iscd);
}