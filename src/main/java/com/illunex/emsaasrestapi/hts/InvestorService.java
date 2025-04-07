package com.illunex.emsaasrestapi.hts;

import com.illunex.emsaasrestapi.common.CustomResponse;
import com.illunex.emsaasrestapi.common.ErrorCode;
import com.illunex.emsaasrestapi.common.code.EnumCode;
import com.illunex.emsaasrestapi.hts.dto.ResponseHtsDTO;
import com.illunex.emsaasrestapi.hts.entity.InvestorTrends;
import com.illunex.emsaasrestapi.hts.repository.InvestorTrendsCustomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class InvestorService {
    private final InvestorTrendsCustomRepository investorTrendsCustomRepository;
    private final ModelMapper modelMapper;

    /**
     * 종목에 대한 투자자 동향 상세 정보(일별)
     * @param pageable
     * @param iscd
     * @param code
     * @return
     */
    public CustomResponse getIvestorTrend(Pageable pageable, String iscd, String code) {
        Page<InvestorTrends> list = investorTrendsCustomRepository.findByInvestorTrends(pageable, iscd);
        if(!iscd.isEmpty() && !code.isEmpty()) {
            List<?> response = new ArrayList<>();
            switch (EnumCode.InvestorTrend.TradingVolumeSmall.codeToEnum(code)) {
                case Investor:
                    response = modelMapper.map(list.getContent(),new TypeToken<List<ResponseHtsDTO.Investor>>() {}.getType());
                    break;
                case Institutional:
                    response = modelMapper.map(list.getContent(),new TypeToken<List<ResponseHtsDTO.Institutional>>() {}.getType());
                    break;
            }
            return CustomResponse.builder()
                    .data(new PageImpl<>(response, list.getPageable(), list.getTotalElements()))
                    .status(ErrorCode.OK.getStatus())
                    .message(ErrorCode.OK.getMessage())
                    .build();
        }
        return CustomResponse.builder()
                .message(ErrorCode.COMMON_INVALID.getMessage())
                .status(ErrorCode.COMMON_INVALID.getStatus())
                .build();
    }

    /**
     * 종목별 투자자 동향 - 기간별 거래량 차트(6개만 보임)
     *  해당월인 시작일인 1일있는 주에 '목요일'에 포함되어있다면 1주차.(주간의 반이 포함되어야 함)
     *  포함되어있지 않다면, 그 다음주 월요일이 1주차.
     *   ex) 2024년 12월 1일의 경우 시작이 일요일이고, 그 주에 목요일에 포함되어있기 때문에 12월 1주차.
     *       2024년 11월 1일의 경우 시작이 금요일이기 때문에 목요일을 포함하고 있지 않기 때문에 그 다음주인 11월 4일이 1주차임.
     * - 집계의 경우 순매수 수량으로만 계산(토스 기준)
     * @param code
     * @return
     */
    public CustomResponse getTradingVolumChart(String iscd, String code) {
        List<ResponseHtsDTO.TradingVolumChart> response = new ArrayList<>();
        if(!code.isEmpty() && !iscd.isEmpty()) {
            for(int i = 5; i >= 0; i--) {
                LocalDate now = LocalDate.now();
                ResponseHtsDTO.TradingVolumChart tradingVolumChart = new ResponseHtsDTO.TradingVolumChart();
                switch (EnumCode.InvestorTrend.PeriodicTradingVolume.codeToEnum(code)) {
                    case Week:
                        LocalDate startDay = now.minusWeeks(i);
                        DayOfWeek monDayOfWeek = startDay.getDayOfWeek();

                        int monDayOfWeekNumber = monDayOfWeek.getValue(); // 월요일: 1, 일요일: 7

                        // 주차를 구하기 위해 시작날짜를 월요일 세팅
                        if(monDayOfWeekNumber != 1) {
                            startDay = startDay.minusDays(monDayOfWeekNumber - 1);
                        }

                        LocalDate endDay = startDay;
                        DayOfWeek friDayOfWeek = endDay.getDayOfWeek();
                        int friDayOfWeekNumber = friDayOfWeek.getValue();

                        // 주차를 구하기 위해 종료날짜를 목요일 세팅
                        if(friDayOfWeekNumber != 4) {
                            endDay = endDay.minusDays(friDayOfWeekNumber);
                            endDay = endDay.plusDays(5);
                        }

                        tradingVolumChart = investorTrendsCustomRepository.findByVolumData(startDay, endDay, iscd);
                        tradingVolumChart.setStartDate(startDay);
                        tradingVolumChart.setEndDate(endDay);

                        ResponseHtsDTO.WeekNumber weekNumber = getCalculateWeekNumber(startDay, endDay);
                        tradingVolumChart.setDate(weekNumber.getNow().getMonthValue()
                                + EnumCode.InvestorTrend.PeriodicTradingVolume.Month.getValue() + " "
                                + weekNumber.getWeekNumber() + EnumCode.InvestorTrend.PeriodicTradingVolume.Week.getValue());
                        break;
                    case Month:
                        now = now.minusMonths(i);
                        tradingVolumChart = investorTrendsCustomRepository.findByVolumData(now.withDayOfMonth(1), now.withDayOfMonth(now.lengthOfMonth()), iscd);
                        tradingVolumChart.setStartDate(now.withDayOfMonth(1));
                        tradingVolumChart.setEndDate(now.withDayOfMonth(now.lengthOfMonth()));
                        tradingVolumChart.setDate(now.getYear() + EnumCode.InvestorTrend.PeriodicTradingVolume.Year.getValue() + " "
                                + now.getMonthValue()
                                + EnumCode.InvestorTrend.PeriodicTradingVolume.Month.getValue());
                        break;
                    case Year:
                        now = now.minusYears(i);
                        tradingVolumChart = investorTrendsCustomRepository.findByVolumData(now.withDayOfYear(1), now.withDayOfYear(now.lengthOfYear()), iscd);
                        tradingVolumChart.setStartDate(now.withDayOfYear(1));
                        tradingVolumChart.setEndDate(now.withDayOfYear(now.lengthOfYear()));
                        tradingVolumChart.setDate(now.getYear() + EnumCode.InvestorTrend.PeriodicTradingVolume.Year.getValue());
                        break;
                }
                tradingVolumChart.setUnitCd(code);
                response.add(tradingVolumChart);
            }
        } else {
            return CustomResponse.builder()
                    .message(ErrorCode.COMMON_INVALID.getMessage())
                    .status(ErrorCode.COMMON_INVALID.getStatus())
                    .build();
        }
        return CustomResponse.builder()
                .data(response)
                .message(ErrorCode.OK.getMessage())
                .status(ErrorCode.OK.getStatus())
                .build();
    }

    /**
     * 주차 계산
     * @param startDay
     * @return
     */
    public static ResponseHtsDTO.WeekNumber getCalculateWeekNumber(LocalDate startDay, LocalDate endDay) {
        ResponseHtsDTO.WeekNumber weekNumber = new ResponseHtsDTO.WeekNumber();

        // 해당 월의 첫날 구하기
        LocalDate firstDayOfMonth = startDay.withDayOfMonth(1);

        // 해당 월 1일이 포함된 주의 '목요일' 날짜 구하기
        LocalDate thursdayOfFirstWeek = firstDayOfMonth.with(DayOfWeek.THURSDAY);

        // 첫 번째 주의 목요일이 그 달에 포함되면, 그 주가 1주차
        if (firstDayOfMonth.isBefore(thursdayOfFirstWeek)) {
            thursdayOfFirstWeek = thursdayOfFirstWeek.minusWeeks(1);
        }

        // 첫 번째 주가 시작되는 월요일 구하기
        LocalDate startOfFirstWeek = thursdayOfFirstWeek.with(DayOfWeek.MONDAY);

        // 시작 날짜가 첫 번째 주보다 이전이면 그 다음 주 월요일이 1주차
        if (startDay.isBefore(startOfFirstWeek)) {
            startOfFirstWeek = startOfFirstWeek.plusWeeks(1);
        }

        // 해당 날짜가 포함된 주차 계산 함수
        weekNumber.setWeekNumber(getWeekNumber(startDay, startOfFirstWeek));
        weekNumber.setNow(startDay);
        return weekNumber;
    }

    /**
     *주차 계산 함수: 월별 기준으로 계산
    */
    public static int getWeekNumber(LocalDate date, LocalDate firstWeekStart) {
        // 날짜가 속한 주의 시작일(Monday)을 구하여 그 주차 계산
        LocalDate startOfWeek = date.with(DayOfWeek.MONDAY);
        // 첫 번째 주의 시작일을 기준으로 몇 번째 주인지 계산
        return (int) java.time.temporal.ChronoUnit.WEEKS.between(firstWeekStart, startOfWeek);
    }
}
