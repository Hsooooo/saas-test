package com.illunex.emsaasrestapi.member.mapper;

import com.illunex.emsaasrestapi.member.vo.MemberLoginHistoryVO;
import com.illunex.emsaasrestapi.member.vo.MemberVO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LoginHistoryMapper {
    Integer insertLoginHistory(MemberLoginHistoryVO vo);
}
