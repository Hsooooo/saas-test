package com.illunex.emsaasrestapi.member.mapper;

import com.illunex.emsaasrestapi.member.vo.MemberVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

@Mapper
public interface MemberMapper {
    Optional<MemberVO> selectByEmail(String email);

}
