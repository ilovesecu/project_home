package com.ilovepc.project_home.web.dhlottery.service;

import com.ilovepc.project_home.web.dhlottery.component.DhlotteryCookieStore;
import com.ilovepc.project_home.web.dhlottery.component.DhlotteryHttpFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@Slf4j
public class DhlotteryBotService {
    private final RestTemplate restTemplate;
    private final DhlotteryHttpFactory httpFactory;
    private final DhlotteryCookieStore cookieStore;
    private final DhlotteryLoginService loginService;

    public DhlotteryBotService(DhlotteryHttpFactory httpFactory,
                               DhlotteryCookieStore cookieStore,
                               DhlotteryLoginService loginService) {
        this.restTemplate = new RestTemplate();
        this.httpFactory = httpFactory;
        this.cookieStore = cookieStore;
        this.loginService = loginService;
    }

    public String getLedger(String userId, String password){
        String targetUrl = "https://www.dhlottery.co.kr/mypage/selectMyLotteryledger.do?srchStrDt=20260215&srchEndDt=20260223&sort=&ltGdsCd=&winResult=&lramSmam=&pageNum=1&recordCountPerPage=10&_=1771694446306";

        List<String> userCookies = cookieStore.getCookies(userId);
        //쿠키가 아예 없으면 바로 로그인
        if(userCookies == null || userCookies.isEmpty()){
            log.info("[{}] 최초 접속, 로그인을 시도합니다.", userId);
            userCookies = loginService.loginAndGetCookie(userId,password);
            if(userCookies == null || userCookies.isEmpty()){
                log.error("로그인에 실패하였습니다. userID:{}", userId);
                return null;
            }
        }

        HttpEntity<String> entity = httpFactory.createEntityWithCookie(userCookies);
        // API 요청 (쿠키가 섞이지 않고 안전하게 전송됨)
        ResponseEntity<String> response = restTemplate.exchange(
                targetUrl,
                HttpMethod.GET,
                entity,
                String.class
        );
        return response.getBody();
    }
}
