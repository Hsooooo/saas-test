package com.illunex.emsaasrestapi.payment.mapper;

import com.illunex.emsaasrestapi.payment.vo.SubscriptionChangeEventVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

@Mapper
public interface SubscriptionChangeEventMapper {

    List<SubscriptionChangeEventVO> selectByLicensePartnershipIdxAndOccurredDate(Integer licensePartnershipIdx, ZonedDateTime startDate, ZonedDateTime endDate);

    List<SubscriptionChangeEventVO> selectByLpAndOccurredBetween(Integer lpIdx, LocalDate start, LocalDate end);

    List<SubscriptionChangeEventVO> selectAddsAfter(Integer licensePartnershipIdx, ZonedDateTime issueDate);

}
