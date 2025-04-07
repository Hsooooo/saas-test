package com.illunex.emsaasrestapi.member.repository;

import com.illunex.emsaasrestapi.member.entity.MemberTermAgree;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberTermAgreeRepository extends JpaRepository<MemberTermAgree, Integer> {
}