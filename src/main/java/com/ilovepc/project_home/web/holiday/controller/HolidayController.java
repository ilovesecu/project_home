package com.ilovepc.project_home.web.holiday.controller;

import com.ilovepc.project_home.web.holiday.service.HolidayService;
import com.ilovepc.project_home.web.holiday.vo.HolidayRequest;
import com.ilovepc.project_home.web.holiday.vo.HolidayRestResponse;
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

    @GetMapping("")
    public List<HolidayRestResponse.Item> getHoliday(HolidayRequest holidayRequest){
        return holidayService.getThisMonthHoliday(holidayRequest);
    }
}
