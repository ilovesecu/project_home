package com.ilovepc.project_home.web.holiday.controller;

import com.ilovepc.project_home.web.holiday.service.HolidayService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/holiday")
@RequiredArgsConstructor
public class HolidayController {
    private final HolidayService holidayService;

    @GetMapping("")
    public void getHoliday(){
        holidayService.getThisMonthHoliday();
    }
}
