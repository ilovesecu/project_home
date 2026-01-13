package com.ilovepc.project_home.config.security.filter;

import com.ilovepc.project_home.common.vo.CommonErrCode;
import com.ilovepc.project_home.config.security.vo.jwt.JwtCode;
import com.ilovepc.project_home.jwt.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

//OncePerRequestFilter : 모든 요청에 대해 한번만 실행됨.
@RequiredArgsConstructor
@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //1. Header에서 토큰을 추출한다.
        String token = jwtUtil.resolveToken(request);

        //2. 토큰 유효성 검사
        if(token != null){
            JwtCode jwtCode = jwtUtil.validateToken(token);
            switch (jwtCode) {
                case ACCESS:
                    // 3. 토큰이 유효하면 인증 정보(Authentication)를 가져와 SecurityContext에 저장
                    Authentication authentication = jwtUtil.getAuthentication(token);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    break;
                case EXPIRED:
                    // [만료] EntryPoint가 알 수 있게 속성 저장
                    request.setAttribute("exception", CommonErrCode.TOKEN_EXPIRED.getCode());
                    break;
                case DENIED:
                    // [오류]
                    request.setAttribute("exception", CommonErrCode.INVALID_TOKEN.getCode());
                    break;
                default:
                    request.setAttribute("exception", CommonErrCode.UNAUTHORIZED.getCode());
                    log.error("정의 되지 않는 오류 내용 {}",jwtCode);
            }
        }
        
        //4. 다음 필터로 진행
        filterChain.doFilter(request, response);
    }
}
