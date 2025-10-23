package com.illunex.emsaasrestapi.payment.mapper;

import com.illunex.emsaasrestapi.payment.vo.SubscriptionChangeEventVO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface SubscriptionChangeEventMapper {

    List<SubscriptionChangeEventVO> selectByLicensePartnershipIdxAndOccurredDate(Integer licensePartnershipIdx, LocalDateTime startDate, LocalDateTime endDate);

    List<SubscriptionChangeEventVO> selectByLpAndOccurredBetween(Integer lpIdx, LocalDateTime start, LocalDateTime end);
}
