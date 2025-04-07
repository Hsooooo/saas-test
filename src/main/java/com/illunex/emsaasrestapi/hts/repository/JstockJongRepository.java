package com.illunex.emsaasrestapi.hts.repository;

import com.illunex.emsaasrestapi.hts.entity.JStockJong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JstockJongRepository extends JpaRepository<JStockJong, Long> {
    JStockJong findTop1ByIscdOrderByCreateDateDesc(String stockCode);
    JStockJong findTop1ByOrderByUpdateDateDesc();
    @Query(value = "select * from jstock_jong where date(update_date) = :date", nativeQuery = true)
    List<JStockJong> findByUpdateDate(String date);
    @Query(value = "select rank from (\n" +
            "     select iscd, row_number() over (order by avls desc) as rank from jstock_jong\n" +
            "     where mrkt_div_cls_code= :mrktDivClsCode and date(create_date) = (select left(max(create_date), 10) from jstock_jong)\n" +
            " )a where iscd = :iscd", nativeQuery = true)
    Long findByIscdOrderByRankDesc(String iscd, String mrktDivClsCode);
}