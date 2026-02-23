package com.ilovepc.project_home.web.dhlottery.component;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DhlotteryHttpFactory {
    /**
     * 쿠키 리스트를 받아서 "User-Agent"와 "Cookie"가 세팅된 HttpEntity를 반환합니다.
     */

    public HttpEntity<String> createEntityWithCookie(List<String> rawCookies) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

        if (rawCookies != null && !rawCookies.isEmpty()) {
            StringBuilder cookieBuilder = new StringBuilder();
            for (String cookie : rawCookies) {
                String pureCookie = cookie.split(";")[0];
                cookieBuilder.append(pureCookie).append("; ");
            }
            headers.add("Cookie", cookieBuilder.toString());
        }

        return new HttpEntity<>(headers);
    }
}
