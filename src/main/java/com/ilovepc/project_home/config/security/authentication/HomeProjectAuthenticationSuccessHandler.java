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
    private final long refreshTokenValidity;

    public HomeProjectAuthenticationSuccessHandler(
            JwtUtil jwtUtil,
            RefreshTokenService refreshTokenService,
            ObjectMapper objectMapper,
            long refreshTokenValidity
    ){
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
        this.objectMapper = objectMapper;
        this.refreshTokenValidity = refreshTokenValidity;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        //1. 인증완료된 사용자 정보
        HomeProjectUserDetails userDetails = (HomeProjectUserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();

        //2. AT & RT 생성
        String accessToken = jwtUtil.createAccessToken(username, userDetails.getAuthorities(), userDetails.getNickname(), userDetails.getUserNo(),userDetails.getEmailVerifiedYn(), userDetails.getCreatedAt());
        String refreshToken = jwtUtil.createRefreshToken(username, userDetails.getAuthorities(), userDetails.getNickname(), userDetails.getUserNo(),userDetails.getEmailVerifiedYn(), userDetails.getCreatedAt());

        //3. Refresh Token을 Redis에 저장
        refreshTokenService.saveToken(username, refreshToken);

        boolean isSecure = request.isSecure();
        //4. Refresh Token을 HttpOnly 쿠키에 담아 전달
        ResponseCookie.ResponseCookieBuilder cookieBuilder = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .path("/api/auth/reissue") // 리프레시 토큰 발급 경로로 제한 (보안 강화)
                .maxAge(refreshTokenValidity / 1000); //7일 간 유효
        if(isSecure){ //HTTPS 환경 (실서버)
            cookieBuilder.secure(true).sameSite("None");
        }else{ //HTTP 환경
            cookieBuilder.path("/");
            cookieBuilder.maxAge(refreshTokenValidity / 1000);
            cookieBuilder.secure(false).sameSite("Lax");
        }
        ResponseCookie cookie = cookieBuilder.build();
        response.addHeader(org.springframework.http.HttpHeaders.SET_COOKIE, cookie.toString());


        //5. Access Token을 JSON 본문으로 응답
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK); // 200 OK

        Map<String, String> tokenResponse = Map.of("accessToken", accessToken);
        response.getWriter().write(objectMapper.writeValueAsString(tokenResponse));
    }

}
