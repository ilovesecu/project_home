package com.ilovepc.project_home.config.security.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilovepc.project_home.common.vo.ApiResponse;
import com.ilovepc.project_home.common.vo.CommonErrCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

/*
 *
 * (401 처리): 인증되지 않은 사용자(혹은 만료된 토큰)가 보호된 리소스에 접근할 때 실행됩니다.
 *
 * */
@RequiredArgsConstructor
@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper om;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        // JwtAuthentication필터에서 담아둔 예외 정보 꺼내기
        String exceptionCode = (String) request.getAttribute("exception");
        if(exceptionCode == null) {
            exceptionCode = CommonErrCode.UNAUTHORIZED.getCode();
        }
        CommonErrCode commonErrCode = CommonErrCode.fromCode(exceptionCode);
        ApiResponse<Void> apiResponse = ApiResponse.fail(Objects.requireNonNull(commonErrCode).getCode(), commonErrCode.getMessage());
        response.setStatus(commonErrCode.getHttpStatus().value()); // 401
        response.setContentType("application/json;charset=UTF-8");

        /*if(CommonErrCode.TOKEN_EXPIRED.getCode().equals(exceptionCode)){
            apiResponse = ApiResponse.fail(CommonErrCode.TOKEN_EXPIRED.getCode(), CommonErrCode.TOKEN_EXPIRED.getMessage());
        }else if(CommonErrCode.INVALID_TOKEN.getCode().equals(exceptionCode)){
            apiResponse = ApiResponse.fail(CommonErrCode.INVALID_TOKEN.getCode(), CommonErrCode.INVALID_TOKEN.getMessage());
        }else{
            apiResponse = ApiResponse.fail(CommonErrCode.UNAUTHORIZED.getCode(), CommonErrCode.UNAUTHORIZED.getMessage());
        }*/
        response.getWriter().write(om.writeValueAsString(apiResponse));
    }
}
