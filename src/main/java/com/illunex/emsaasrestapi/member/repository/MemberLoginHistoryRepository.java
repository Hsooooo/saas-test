package com.illunex.emsaasrestapi.member.repository;

import com.illunex.emsaasrestapi.member.entity.MemberLoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberLoginHistoryRepository extends JpaRepository<MemberLoginHistory, Integer> {
}