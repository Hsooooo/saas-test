package com.illunex.emsaasrestapi.member.mapper;

import com.illunex.emsaasrestapi.member.vo.MemberTermAgreeVO;
import com.illunex.emsaasrestapi.member.vo.MemberTermVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

@Mapper
public interface MemberTermAgreeMapper {
    void insertByMemberTermAgreeVO(MemberTermAgreeVO memberTermAgreeVO);
}
