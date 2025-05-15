package com.illunex.emsaasrestapi.member.mapper;

import com.illunex.emsaasrestapi.member.vo.MemberEmailHistoryVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

@Mapper
public interface EmailHistoryMapper {
    void insertByMemberEmailHistoryVO(MemberEmailHistoryVO memberEmailHistoryVO);
    Optional<MemberEmailHistoryVO> selectByMemberIdxAndCertDataAndEmailType(Integer memberIdx, String certData, String emailType);

    Optional<MemberEmailHistoryVO> selectTop1ByMemberIdxAndEmailTypeOrderByCreateDateDesc(Integer memberIdx, String emailType);
    Optional<MemberEmailHistoryVO> selectByCertData(String certData);
    void updateUsedByIdx(boolean used, Long idx);
}
