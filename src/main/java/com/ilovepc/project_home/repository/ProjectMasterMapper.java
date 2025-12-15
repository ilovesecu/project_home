package com.ilovepc.project_home.repository;

import com.ilovepc.project_home.config.rdb.annotation.HomeMaster;
import com.ilovepc.project_home.web.holiday.vo.HolidayRestResponse;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@HomeMaster
public interface ProjectMasterMapper {
    int insertHoliday(@Param("holidayInfoList") List<HolidayRestResponse.Item>holidayInfoList);

    List<HolidayRestResponse.Item> getHolidayInfo(@Param("startDate")String startDate, @Param("endDate")String endDate);
}
