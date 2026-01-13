package com.ilovepc.project_home.config.security.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilovepc.project_home.common.vo.ApiResponse;
import com.ilovepc.project_home.common.vo.CommonErrCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/*
*
* (403 처리): 로그인(인증)은 성공했지만, ROLE_ADMIN 페이지에 ROLE_USER가 접근하는 경우 등에 호출
*
* */
@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {
    private final ObjectMapper om;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        CommonErrCode commonErrCode = CommonErrCode.ACCESS_DENIED;
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(commonErrCode.getHttpStatus().value()); // 403

        ApiResponse<Void> apiResponse = ApiResponse.fail(commonErrCode.getCode(), commonErrCode.getMessage());
        response.getWriter().write(om.writeValueAsString(apiResponse));
    }
}
