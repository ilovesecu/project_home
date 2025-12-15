package com.ilovepc.project_home.web.holiday.service;

import com.ilovepc.project_home.repository.ProjectMasterMapper;
import com.ilovepc.project_home.web.holiday.vo.HolidayMonthRequest;
import com.ilovepc.project_home.web.holiday.vo.HolidayRestResponse;
import com.ilovepc.project_home.web.holiday.vo.HolidayTodayRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class HolidayService {
    //공공데이터포털 인증키
    @Value("${apis.spcdeEncode}")
    private String SERVICE_KEY;
    private static final String API_URL = "https://apis.data.go.kr/B090041/openapi/service/SpcdeInfoService/getRestDeInfo";
    private final ProjectMasterMapper projectMasterMapper;

    public HolidayRestResponse getHolidaysFromAPI(int year, int month){
        //RestTemplate 기본설정을 Encode 안하는 설정으로 변경
        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory();
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE); //절대 인코딩 건들지 마!

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setUriTemplateHandler(factory); // 조작된 설정을 넣기

        // 요청 URL 생성
        String monthStr = String.format("%02d", month); //1,2,3 --> 01,02,03 두 자리 포맷 변경
        String uriString = UriComponentsBuilder.fromUriString(API_URL)
                .queryParam("serviceKey", SERVICE_KEY)
                .queryParam("solYear", year)
                .queryParam("solMonth", monthStr)
                .queryParam("_type", "json")
                .queryParam("numOfRows", 100) //해당 월에 휴일이 100개가 넘을 수가 없으니 이렇게만 해놔도 될듯?
                .build(true) //이미 인코딩했다면 url (Encoding키를 사용했을 떄)
                .toUriString();

        try{
            HolidayRestResponse response = restTemplate.getForObject(uriString, HolidayRestResponse.class);
            saveHoliday(response);
            return response;
        }catch (Exception e){
            log.error("[getHolidays] 에러 발생 param - year:{}, month:{}",year, month, e);
        }
        return null;
    }

    //이번달 휴일 가져오기 --> 파라미터가 없으면 이번달, 있으면 파라미터에 적힌 날짜의 휴일가져오기로 변경
    public List<HolidayRestResponse.Item> getThisMonthHoliday(HolidayMonthRequest holidayMonthRequest){
        if(!holidayMonthRequest.validate()){ //파라미터가 정확하지 않다면 오늘날짜 강제 셋팅
            LocalDate today = LocalDate.now();
            int year = today.getYear();
            int month = today.getMonthValue();

            holidayMonthRequest.setYear(year);
            holidayMonthRequest.setMonth(month);
        }
        return getHolidays(holidayMonthRequest.getYear(), holidayMonthRequest.getMonth());
    }

    //이번 달 휴일을 DB에서 검색한다.
    public List<HolidayRestResponse.Item> getHolidaysFromDB(String startDate, String endDate){
        List<HolidayRestResponse.Item> holidayInfo = projectMasterMapper.getHolidayInfo(startDate, endDate);
        return holidayInfo;
    }

    //DB에 휴일 저장
    public void saveHoliday(HolidayRestResponse holidayRestResponse){
        if(holidayRestResponse == null || holidayRestResponse.isEmpty()) return ;
        int insertCnt = projectMasterMapper.insertHoliday(holidayRestResponse.getHolidayItem());
        log.info("holiday insert cnt:{}",insertCnt);
    }

    //현재날짜가 휴일인지 판별한다.
    public HolidayRestResponse.Item holidayToday(HolidayTodayRequest holidayTodayRequest){
        if(!holidayTodayRequest.validate()){
            //파라미터가 정확하지 않다면 오늘날짜를 강제로 셋팅해준다.
            LocalDate today = LocalDate.now();
            int year = today.getYear();
            int month = today.getMonthValue();
            int day = today.getDayOfMonth();

            holidayTodayRequest.setYear(year);
            holidayTodayRequest.setMonth(month);
            holidayTodayRequest.setDay(day);
        }
        List<HolidayRestResponse.Item> holidays = getHolidays(holidayTodayRequest.getYear(), holidayTodayRequest.getMonth());
        /*boolean b = holidays.stream().anyMatch((response -> {
            return "Y".equals(response.getIsHoliday().toUpperCase(Locale.ROOT)) && Objects.equals(response.getLocdate(), holidayTodayRequest.integrated());
        }));*/
        HolidayRestResponse.Item item = holidays.stream().filter((response -> {
            return "Y".equals(response.getIsHoliday().toUpperCase(Locale.ROOT)) && Objects.equals(response.getLocdate(), holidayTodayRequest.integrated());
        })).findFirst().orElse(null);
        return item;
    }
    
    private List<HolidayRestResponse.Item>  getHolidays(int year, int month){
        YearMonth yearMonth = YearMonth.of(year, month);
        String endDate = yearMonth.atEndOfMonth().toString();
        String startDate = yearMonth.atDay(1).toString();
        List<HolidayRestResponse.Item> holidaysFromDB = getHolidaysFromDB(startDate, endDate);

        //DB에 값이 없다면 API CALL 및 DB 저장
        if(holidaysFromDB == null || holidaysFromDB.isEmpty()){
            HolidayRestResponse holidays = getHolidaysFromAPI(year, month);
            return holidays.getHolidayItem();
        }

        return  holidaysFromDB; //DB값이 있다면 DB값을 반환한다.
    }
}
