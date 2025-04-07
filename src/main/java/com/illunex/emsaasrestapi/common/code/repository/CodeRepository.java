package com.illunex.emsaasrestapi.common.code.repository;

import com.illunex.emsaasrestapi.common.code.entity.Code;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CodeRepository extends JpaRepository<Code, String> {
    List<Code> findAllByThirdCodeOrderByCode(String thirdCode);
    List<Code> findAllByCodeStartingWithAndThirdCodeNotOrderBySeqAsc(String firstSecondCode, String thirdCode);
    List<Code> findByFirstCodeAndSeqOrderByCodeAsc(String firstCode, int seq);
    Code findByFirstCodeAndSecondCodeAndSeq(String firstCode, String thirdCode, int seq);
    List<Code> findAllByFirstCode(String code);
}
