package com.illunex.emsaasrestapi.partnership.mapper;

import com.illunex.emsaasrestapi.partnership.dto.RequestPartnershipDTO;
import com.illunex.emsaasrestapi.partnership.vo.PartnershipMemberViewVO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Mapper
public interface PartnershipMemberViewMapper {
    List<PartnershipMemberViewVO> selectAllBySearchMemberAndPageable(RequestPartnershipDTO.SearchMember searchMember, Pageable pageable);
    Integer countAllBySearchMember(RequestPartnershipDTO.SearchMember searchMember);
}
