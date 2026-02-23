package com.ilovepc.project_home.web.dhlottery.component;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DhlotteryCookieStore {
    /*
    WMONID=쿠키내용; Expires=Mon, 22-Feb-2027 02:10:18 GMT; Path=/
    DHJSESSIONID=쿠키내용; Domain=.dhlottery.co.kr; Path=/; Secure; HttpOnly; SameSite=None
    */
    private final Map<String, List<String>> store = new ConcurrentHashMap<>();

    public void saveCookies(String userId, List<String> cookies){
        store.put(userId, cookies);
    }

    public List<String> getCookies(String userId) {
        return store.get(userId);
    }

    public void clearCookies(String userId) {
        store.remove(userId);
    }
}
