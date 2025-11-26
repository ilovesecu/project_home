package com.ilovepc.project_home.common.utils;

import com.ilovepc.project_home.config.security.resolver.LiteDeviceResolver;
import com.ilovepc.project_home.config.security.vo.UserAgent;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.mobile.device.DevicePlatform;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ClientUtils {
    public static String getClientIp(HttpServletRequest request) {
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.length() == 0 || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getHeader("Proxy-Client-IP");
        }
        if (clientIp == null || clientIp.length() == 0 || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getHeader("WL-Proxy-Client-IP");
        }
        if (clientIp == null || clientIp.length() == 0 || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getRemoteAddr(); // 기본 IP
        }

        // 프록시를 거치는 경우 첫 번째 IP를 반환
        if (clientIp != null && clientIp.contains(",")) {
            clientIp = clientIp.split(",")[0].trim();
        }

        return clientIp;
    }

    public static UserAgent getUserAgent(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        String newDomain = "HOME";
        if (userAgent != null) {
            if (userAgent.contains(newDomain)) {
                String sAgentInfo = userAgent.substring(userAgent.indexOf(newDomain));
                if (sAgentInfo.lastIndexOf(newDomain) > 0) {
                    sAgentInfo = sAgentInfo.substring(sAgentInfo.lastIndexOf(newDomain));
                }
                String[] data = sAgentInfo.split("\\|");
                return Optional.ofNullable(data).filter(a -> a.length >= 6).map(item -> {
                    UserAgent homeAgent = new UserAgent();
                    homeAgent.setDeviceType((new LiteDeviceResolver().resolveDevice(request).getDevicePlatform()));
                    homeAgent.setServiceType(item[1]);

                    if (String.valueOf(homeAgent.getDeviceType()).equals("UNKNOWN")) {
                        homeAgent.setDeviceType(item[1].equals("a") ? DevicePlatform.ANDROID : DevicePlatform.IOS);
                    }

                    homeAgent.setDeviceId(item[2]);
                    homeAgent.setSmtpToken(item[3]);
                    homeAgent.setAppVersion(item[4]);
                    homeAgent.setPhoneVersion(item[5]);
                    homeAgent.setAppProvider(item[1].equals("a") ? item[6] : "appstore");
                    homeAgent.setIAppVersion(getAppVersionNumber(homeAgent));        //예 : 1.5.130 ==> 130 넘겨줌.
                    return homeAgent;
                }).orElse(new UserAgent());
            }
        }
        // web개발시에 편의를 위해 필요
        UserAgent webAgent = new UserAgent();
        webAgent.setAppProvider("notAPP");
        webAgent.setDeviceType(DevicePlatform.UNKNOWN);
        webAgent.setDeviceId("notAPP");
        webAgent.setAppVersion("notAPP");
        webAgent.setPhoneVersion("notAPP");
        webAgent.setIAppVersion(0);
        return webAgent;
    }

    public static int getAppVersionNumber(UserAgent agent){
        int result = 0;
        if (agent != null) {
            result = getAppNumber(agent.getAppVersion(), "\\.");
        }
        return result;
    }


    public static int getAppNumber (String appVersion, String pattern) {
        int iRet = 0;
        List<String> appVersionList = Arrays.asList(appVersion.split(pattern));
        if(appVersionList != null && appVersionList.size() == 3){
            iRet = Integer.parseInt(appVersionList.get(1))*10000 + Integer.parseInt(appVersionList.get(2));
        }
        return iRet;
    }
}
