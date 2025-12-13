package com.ilovepc.project_home.web.holiday.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class HolidayService {
    //공공데이터포털 인증키
    @Value("${apis.spcdeEncode}")
    private String SERVICE_KEY;
    private static final String API_URL = "https://apis.data.go.kr/B090041/openapi/service/SpcdeInfoService/getRestDeInfo";

    public List<String> getHolidays(int year, int month){
        //RestTemplate 기본설정을 Encode 안하는 설정으로 변경
        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory();
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE); //"절대 인코딩 건들지 마!"

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setUriTemplateHandler(factory); // 조작된 설정을 넣기

        // 요청 URL 생성
        String monthStr = String.format("%02d", month); //1,2,3 --> 01,02,03 두 자리 포맷 변경
        String uriString = UriComponentsBuilder.fromUriString(API_URL)
                .queryParam("serviceKey", SERVICE_KEY)
                .queryParam("solYear", year)
                .queryParam("solMonth", monthStr)
                .queryParam("_type", "json")
                .queryParam("numOfRows", 100)
                .build(true) //이미 인코딩했다면 url (Encoding키를 사용했을 떄)
                .toUriString();

        try{
            String response = restTemplate.getForObject(uriString, String.class);
            log.error("response:{}",response);
            return new ArrayList<>();
        }catch (Exception e){
            log.error("[getHolidays] 에러 발생",e);
        }
        return null;
    }

    //이번달 휴일 가져오기
    public boolean getThisMonthHoliday(){
        LocalDate today = LocalDate.now();
        int year = today.getYear();
        int month = today.getMonthValue();

        List<String> holidays = getHolidays(year, month);
        String todayStr = today.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return true;
    }
}
