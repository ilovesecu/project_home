package com.ilovepc.project_home.web.holiday.vo;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class HolidayTodayRequest {
    private int year;
    private int month;
    private int day;

    public boolean validate(){
        if(year <1)return false;
        if(month < 1 || month > 12){ return false;}
        if(day < 1 || day > 31){ return false;}
        return true;
    }

    public Integer integrated(){
        return year * 10000 + month * 100 + day;
    }
}
