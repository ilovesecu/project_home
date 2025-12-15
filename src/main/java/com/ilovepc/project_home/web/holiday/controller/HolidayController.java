package com.ilovepc.project_home.web.holiday.controller;

import com.ilovepc.project_home.web.holiday.service.HolidayService;
import com.ilovepc.project_home.web.holiday.vo.HolidayMonthRequest;
import com.ilovepc.project_home.web.holiday.vo.HolidayRestResponse;
import com.ilovepc.project_home.web.holiday.vo.HolidayTodayRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/holiday")
@RequiredArgsConstructor
public class HolidayController {
    private final HolidayService holidayService;

    @GetMapping("/month")
    public List<HolidayRestResponse.Item> getHoliday(HolidayMonthRequest holidayMonthRequest){
        //is_holiday = 'y'여야 진짜 휴일임!
        return holidayService.getThisMonthHoliday(holidayMonthRequest);
    }

    @GetMapping("/today")
    public HolidayRestResponse.Item isHoliday(@Valid HolidayTodayRequest holidayTodayRequest){
        //단순히 해당 날짜가 휴일인지 체크한다.
        return holidayService.holidayToday(holidayTodayRequest);
    }
}
