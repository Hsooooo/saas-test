package com.illunex.emsaasrestapi.member.repository;

import com.illunex.emsaasrestapi.member.entity.MemberEmailHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberPasswordHistoryRepository extends JpaRepository<MemberEmailHistory, Integer> {
    Optional<MemberEmailHistory> findTop1ByMemberIdxAndEmailTypeOrderByCreateDateDesc(Integer memberIdx, String emailType);
}