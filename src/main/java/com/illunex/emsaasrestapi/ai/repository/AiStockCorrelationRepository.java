package com.illunex.emsaasrestapi.ai.repository;

import com.illunex.emsaasrestapi.ai.dto.ResponseAiMapper;
import com.illunex.emsaasrestapi.ai.entity.AiStockCorrelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiStockCorrelationRepository extends JpaRepository<AiStockCorrelation, Long> {
    @Query("select " +
                "c.destStockCode as stockCode, " +
                "c.stockDate as stockDate, " +
                "c.correlation as correlation, " +
                "c.calcRange as calcRange, " +
                "c.lag as lag, " +
                "c.destStockClose as stockClose, " +
                "c.destStockIncRate as stockIncRate, " +
                "c.korIsnm as korIsnm, " +
                "group_concat(d.themeName) as themeNames " +
            "from (" +
                "select " +
                    "distinct a.destStockCode as destStockCode, " +
                    "a.stockDate as stockDate, " +
                    "a.correlation as correlation, " +
                    "a.calcRange as calcRange, " +
                    "a.lag as lag, " +
                    "a.destStockClose as destStockClose, " +
                    "a.destStockIncRate as destStockIncRate, " +
                    "b.korIsnm as korIsnm " +
                "from (" +
                    "select " +
                        "t1.destStockCode as destStockCode, " +
                        "t1.stockDate as stockDate, " +
                        "t1.correlation as correlation, " +
                        "t1.calcRange as calcRange, " +
                        "t1.lag as lag, " +
                        "t1.destStockClose as destStockClose, " +
                        "t1.destStockIncRate as destStockIncRate " +
                    "from AiStockCorrelation t1 " +
                    "where t1.sourceStockCode = :stockCode " +
                    "and t1.calcRange = :calcRange " +
                    "order by t1.stockDate desc " +
                    "limit 18446744073709551615" +
                ") a " +
                "inner join JStockJong b " +
                "on a.destStockCode = b.iscd " +
                "and a.stockDate = (" +
                    "select " +
                        "max(t2.stockDate) as stockDate " +
                    "from AiStockCorrelation t2" +
                ") " +
                "group by b.iscd, a.destStockCode, a.lag " +
                "order by a.stockDate desc, a.calcRange asc, a.correlation desc " +
                "limit :limit" +
            ") c " +
            "inner join ThemeOriginal d " +
            "on c.destStockCode = d.iscd " +
            "group by d.iscd " +
            "order by c.correlation desc")
    List<ResponseAiMapper.AiCorrelationInterface> getCorrelationLimit(String stockCode, Integer calcRange, Integer limit);
}