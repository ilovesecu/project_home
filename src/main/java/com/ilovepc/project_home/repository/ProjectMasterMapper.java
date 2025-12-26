package com.ilovepc.project_home.repository;

import com.ilovepc.project_home.config.rdb.annotation.HomeMaster;
import com.ilovepc.project_home.web.holiday.vo.HolidayRestResponse;
import com.ilovepc.project_home.web.todo.vo.TodoAddKeywordParam;
import com.ilovepc.project_home.web.todo.vo.TodoAddKeywordResult;
import com.ilovepc.project_home.web.todo.vo.TodoAddTaskParam;
import com.ilovepc.project_home.web.todo.vo.TodoKeywordResult;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@HomeMaster
public interface ProjectMasterMapper {
    int insertHoliday(@Param("holidayInfoList") List<HolidayRestResponse.Item>holidayInfoList);

    List<HolidayRestResponse.Item> getHolidayInfo(@Param("startDate")String startDate, @Param("endDate")String endDate);


    // 1. 키워드 조회 (이미 존재하는지 확인용)
    // 삭제되지 않은(is_deleted = 0) 키워드만 조회
    TodoAddKeywordResult findKeyword(@Param("name") String name);

    // 2. 키워드 등록
    // useGeneratedKeys를 통해 insert 후 id값을 객체에 채워줌
    void insertKeyword(TodoAddKeywordParam todoKeyword);

    // 3. 할 일 단건 등록
    void insertTask(TodoAddTaskParam todoTask);

    // 4. 할 일 여러 개 한 번에 등록 (List로 받을 경우)
    // 예: /ta 다이소 냄비,휴지,물티슈
    int insertTasksBulk(@Param("taskList") List<TodoAddTaskParam> taskList);

    List<TodoKeywordResult> selectTasksByKeywords(@Param("keywordList")List<String>keywordList);
}
