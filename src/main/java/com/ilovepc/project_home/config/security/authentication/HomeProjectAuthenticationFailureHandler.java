package com.ilovepc.project_home.config.security.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilovepc.project_home.common.exception.AuthenticationFailException;
import com.ilovepc.project_home.common.vo.ApiResponse;
import com.ilovepc.project_home.common.vo.CommonErrCode;
import com.ilovepc.project_home.common.vo.ErrCode;
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

        ErrCode errCode = CommonErrCode.UNKNOWN_ERROR;

        //우리가 만든 커스텀 예외 확인
        if(exception instanceof AuthenticationFailException authException) {
            errCode = authException.getErrCode();
        }
        //비밀번호 틀림 (기본예외)
        else if(exception instanceof BadCredentialsException){
            errCode = CommonErrCode.BAD_CREDENTIALS;
        }


        //JSON Response Body 작성
        response.setStatus(errCode.getHttpStatus().value());
        response.setContentType("application/json;charset=UTF-8");
        ApiResponse<Void> apiResponse = ApiResponse.fail(errCode.getCode(), errCode.getMessage());
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}
