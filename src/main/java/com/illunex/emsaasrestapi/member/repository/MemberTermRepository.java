package com.illunex.emsaasrestapi.member.repository;

import com.illunex.emsaasrestapi.member.entity.MemberTerm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemberTermRepository extends JpaRepository<MemberTerm, Integer> {
    List<MemberTerm> findAllByActiveTrue();
}