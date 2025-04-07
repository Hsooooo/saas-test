package com.illunex.emsaasrestapi.hts.repository;

import com.illunex.emsaasrestapi.hts.dto.ResponseHtsMapper;
import com.illunex.emsaasrestapi.hts.entity.KsicCompany;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository

public interface KsicCompanyRepository extends JpaRepository<KsicCompany, Long> {
    @Query(value = "select ksic_idx as ksicIdx, sum(prdy_ctrt)/count(*)as prdyCtrt from ksic_company where ksic_idx = :idx", nativeQuery = true)
    ResponseHtsMapper.KsicCompanyGroupInterface findByKsicIdxCustom(Long idx);
    KsicCompany findTop1ByKsicIdx(Long ksicIdx);
    KsicCompany findByIscd(String iscd);
    List<KsicCompany> findAllByKsicIdx(Long ksicCategoryIdx, Pageable pageable);
    Long countAllByKsicIdx(Long ksicCategoryIdx);

    @Query(value = "select (select COALESCE(max(idx)+1,1) from `em_stock`.`ksic_company`) as idx, b.id as company_id, " +
            "           b.business_category_code as company_business_category_code,a.iscd, a.kor_isnm,\n" +
            "           c.idx as ksic_idx,\n" +
            "           c.code as ksic_code, c.code_desc as ksic_desc, :prdyCtrt as prdy_ctrt, now() as scrap_date from(\n" +
            "        select iscd, kor_isnm from jstock_jong\n" +
            "        where date(update_date) = :scrapDate and iscd = :iscd \n" +
            "    )a inner join (select iscd, business_category_code, id from company_info group by iscd) b on a.iscd = b.iscd\n" +
            "       inner join ksic_category c on left(b.business_category_code,3)=c.code;\n", nativeQuery = true)
    KsicCompany selectCustomKsicCompany(String scrapDate, String iscd, Double prdyCtrt);
}