package com.illunex.emsaasrestapi.member.mapper;

import com.illunex.emsaasrestapi.member.vo.MemberVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface MemberMapper {
    Optional<MemberVO> selectByEmail(String email);
    int updateNameByIdx(@Param("name") String name,@Param("idx") Integer memberIdx);
    Optional<MemberVO> selectByIdx(int memberIdx);
    void updateMemberStateByIdx(Integer idx, String stateCd);
    void updateStateAndPasswordByIdx(Integer idx, String stateCd, String password);
    List<MemberVO> selectByProjectIdx(Integer ProjectIdx);
}
