package com.illunex.emsaasrestapi.member.mapper;

import com.illunex.emsaasrestapi.member.vo.MemberTermVO;
import com.illunex.emsaasrestapi.member.vo.MemberVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface MemberTermMapper {
    List<MemberTermVO> selectAllByActiveTrue();
    Optional<MemberTermVO> selectByIdx(Integer idx);
}
