package com.ilovepc.project_home.web.holiday.vo;

import lombok.Data;

@Data
public class HolidayRequest {
    private int year;
    private int month;

    public boolean validate(){
        if(year == 0) return false;
        if(month == 0) return false;
        return true;
    }
}
