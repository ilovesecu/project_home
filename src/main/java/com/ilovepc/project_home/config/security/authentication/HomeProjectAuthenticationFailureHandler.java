package com.ilovepc.project_home.config.security.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilovepc.project_home.common.exception.AuthenticationFailException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HomeProjectAuthenticationFailureHandler implements AuthenticationFailureHandler {
    private final ObjectMapper objectMapper;

    public HomeProjectAuthenticationFailureHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        // 응답설정 HTTP 401
        response.setContentType("applicaiton/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        String errCode = "AUTH_FAIL";
        String errMessage = "인증 실패";

        //우리가 만든 커스텀 예외 확인
        if(exception instanceof AuthenticationFailException authException) {
            errCode = authException.getErrorCode();
            errMessage = authException.getMessage();
        }
        //비밀번호 틀림 (기본예외)
        else if(exception instanceof BadCredentialsException){
            errCode = BadCredentialsException.class.getName();
        }

        if(exception != null){
            errMessage = exception.getMessage(); //예: BadCredentialsException에서 설정한 메시지
        }

        //JSON Response Body 작성
        Map<String,Object> errResponseMap = new HashMap<>();
        errResponseMap.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        errResponseMap.put("error", "Unauthorized");
        errResponseMap.put("message", errMessage);
        errResponseMap.put("path",request.getRequestURI());

        response.getWriter().write(objectMapper.writeValueAsString(errResponseMap));
    }
}
