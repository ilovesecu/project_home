package com.ilovepc.project_home.web.auth.service;

import com.ilovepc.project_home.config.security.HomeProjectUserDetailsService;
import com.ilovepc.project_home.config.security.vo.HomeProjectUserDetails;
import com.ilovepc.project_home.jwt.service.RefreshTokenService;
import com.ilovepc.project_home.jwt.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final HomeProjectUserDetailsService homeProjectUserDetailsService;

    // 리프래시 토큰으로 엑세스 토큰 재발급
    public ResponseEntity<?> reissueAccessToken(HttpServletRequest request){
        String username = null;
        String oldRefreshToken = null;

        // 1. 쿠키에서 RefreshToken 추출
        Cookie[] cookies = request.getCookies();
        if(cookies != null){
            for(Cookie cookie : cookies){
                if(cookie.getName().equals("refreshToken")){
                    oldRefreshToken = cookie.getValue();
                }
            }
        }

        if(oldRefreshToken == null){
            return ResponseEntity.status(401).body("Refresh token is mission");
        }

        //2. 토큰에서 사용자 아이디 추출 --> 리프레시 토큰이 만료되었어도 Claims는 읽기 가능하긴함.
        try{
            username = jwtUtil.getUsernameFromToken(oldRefreshToken);
        }catch (Exception e){
            return ResponseEntity.status(401).body("Invalid refresh token");
        }

        //3. Redis 토큰과 비교
        String redisRefreshToken = refreshTokenService.findToken(username);
        if(redisRefreshToken == null || !redisRefreshToken.equals(oldRefreshToken)){
            return ResponseEntity.status(401).body("Refresh token mismatch or expired");
        }

        //4. 인증성공 - 새로운 AccessToken 생성
        HomeProjectUserDetails userDetails = (HomeProjectUserDetails)homeProjectUserDetailsService.loadUserByUsername(username);
        String accessToken = jwtUtil.createAccessToken(userDetails.getUsername(), userDetails.getAuthorities(), userDetails.getNickname(), userDetails.getUserNo(), userDetails.getEmailVerifiedYn(), userDetails.getCreatedAt());
        return ResponseEntity.ok(Map.of("accessToken", accessToken));
    }
}
