package com.ilovepc.project_home.config.security.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilovepc.project_home.config.security.vo.HomeProjectUserDetails;
import com.ilovepc.project_home.jwt.service.RefreshTokenService;
import com.ilovepc.project_home.jwt.util.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.util.Map;


public class HomeProjectAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final ObjectMapper objectMapper;

    public HomeProjectAuthenticationSuccessHandler(
            JwtUtil jwtUtil,
            RefreshTokenService refreshTokenService,
            ObjectMapper objectMapper
    ){
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        //1. 인증완료된 사용자 정보
        HomeProjectUserDetails userDetails = (HomeProjectUserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();

        //2. AT & RT 생성
        String accessToken = jwtUtil.createAccessToken(username, userDetails.getAuthorities());
        String refreshToken = jwtUtil.createRefreshToken(username, userDetails.getAuthorities());

        //3. Refresh Token을 Redis에 저장
        refreshTokenService.saveToken(username, refreshToken);

        //4. Refresh Token을 HttpOnly 쿠키에 담아 전달
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true) //HTTPS환경에서만 사용
                .path("/api/auth/reissue")
                .maxAge(60 * 60 * 24 * 7) //7일 간 유효
                .build();

        //5. Access Token을 JSON 본문으로 응답
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK); // 200 OK

        Map<String, String> tokenResponse = Map.of("accessToken", accessToken);
        response.getWriter().write(objectMapper.writeValueAsString(tokenResponse));
    }

}
