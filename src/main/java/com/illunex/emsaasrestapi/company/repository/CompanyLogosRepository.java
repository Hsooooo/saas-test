package com.illunex.emsaasrestapi.company.repository;

import com.illunex.emsaasrestapi.company.entity.CompanyLogos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompanyLogosRepository extends JpaRepository<CompanyLogos, Long> {
    List<CompanyLogos> findAllByIscdIn(List<String> iscds);
}