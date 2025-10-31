package com.illunex.emsaasrestapi.payment.util;

import lombok.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class BillingAnchorPolicy {

    @Value
    public static class Period {
        LocalDate start; // inclusive
        LocalDate end;   // exclusive
    }

    /** 내일부터 시작하는 1개월 주기(기존 billing_day 재사용) */
    public Period resolveNextPeriodFrom(LocalDate startInclusive, int billingDay) {
        // anchor = billingDay, startInclusive >= anchor 라면 다음 anchor로
        // 간단 구현: [startInclusive, startInclusive + 1개월) 배타 종료
        LocalDate endExcl = startInclusive.plusMonths(1);
        // 2월 단축 등은 달력 자체가 처리
        return new Period(startInclusive, endExcl);
    }
}