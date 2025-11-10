package com.ilovepc.project_home.config.security.filter;

import com.ilovepc.project_home.jwt.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

//OncePerRequestFilter : 모든 요청에 대해 한번만 실행됨.
@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //1. Header에서 토큰을 추출한다.
        String token = jwtUtil.resolveToken(request);

        //2. 토큰 유효성 검사
        if(token != null && jwtUtil.validateToken(token)){
            // 3. 토큰이 유효하면 인증 정보(Authentication)를 가져와 SecurityContext에 저장
            Authentication authentication = jwtUtil.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        
        //4. 다음 필터로 진행
        filterChain.doFilter(request, response);
    }
}
