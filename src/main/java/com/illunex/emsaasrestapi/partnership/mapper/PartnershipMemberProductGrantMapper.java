package com.illunex.emsaasrestapi.partnership.mapper;

import com.illunex.emsaasrestapi.partnership.vo.PartnershipMemberProductGrantVO;
import com.illunex.emsaasrestapi.partnership.vo.PartnershipPositionVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

@Mapper
public interface PartnershipMemberProductGrantMapper {
    void insertByPartnershipMemberProductGrantVO(PartnershipMemberProductGrantVO partnershipMemberProductGrantVO);
    List<PartnershipMemberProductGrantVO> selectByPartnershipMemberIdx(Integer partnershipMemberIdx);
}
