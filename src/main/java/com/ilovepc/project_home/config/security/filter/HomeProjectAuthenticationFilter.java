package com.ilovepc.project_home.config.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilovepc.project_home.web.auth.vo.signin.SignInRequest;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

/*
구분	        방법 A: Controller 방식 (질문의 AuthController)    	방법 B: Filter 방식 (님이 만든 SISOAuthenticationFilter)
로그인 위치	Controller (/api/auth/login) 메서드 안	            Spring Security Filter Chain 앞단
인증 담당	개발자가 직접 passwordEncoder.matches() 작성	        **AuthenticationManager**에게 위임 (자동)
성공 후 처리	Controller에서 토큰 만들어서 리턴	                    **SuccessHandler**에서 토큰 만들어서 리턴
특징	        코드가 직관적이나 Security의 강력한 기능을 100% 못 씀	    Security 표준 구조. 확장성 좋음 (권장)




2. JWT 환경에서의 두 가지 케이스
보통 JWT 프로젝트에서는 이 구조가 "최초 로그인(토큰 발급)" 시에 주로 사용됩니다.

로그인 시 (토큰 발급):

사용자 ID/PW 전송 -> SISOAuthenticationFilter 동작 -> Provider가 DB 확인 -> 성공 시 JWT 발급. (작성하신 코드가 이 역할을 수행합니다.)

API 요청 시 (토큰 검증):

헤더에 JWT 포함 전송 -> JwtAuthorizationFilter(별도 작성 필요) 동작 -> 토큰 파싱 -> SecurityContext에 강제 주입.

참고: API 요청 시에는 성능을 위해 AuthenticationManager를 거치지 않고 필터에서 바로 검증 후 끝내는 경우가 많습니다.


 */
public class HomeProjectAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    /*여기서 사용자의 로그인 요청을 가로채서 AuthenticationManager에게 전달!*/
    private final ObjectMapper objectMapper;


    // 어느 URL로 요청이 왔을 때 이 필터를 동작시킬지 설정
    public HomeProjectAuthenticationFilter(ObjectMapper objectMapper) {
        super("/api/login"); // 로그인 엔드포인트 설정
        this.objectMapper = objectMapper;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        // 1. HTTP Method 검사 (POST만 허용)
        if (!request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        }

        // 2. JSON 데이터를 DTO로 변환
        SignInRequest loginRequest = objectMapper.readValue(request.getInputStream(), SignInRequest.class);

        // 3. 인증 토큰 생성 (아직 인증 안 된 상태)
        UsernamePasswordAuthenticationToken authRequest =
                UsernamePasswordAuthenticationToken.unauthenticated(loginRequest.getEmail(), loginRequest.getPassword());

        // 4. 부가 정보 설정 (IP, 세션 ID 등)
        setDetails(request, authRequest);

        // 로그인 이전에 로그아웃을 여기서 해준다.
        //sisoPacketService.sendPacket("logout",principal.getMemNo(),"{}");
        //sisoPacketService.sendMultiChatPacket("logout",principal.getMemNo(),"{}");


        // 5. AuthenticationManager에게 검증 요청 -> Provider로 넘어감
        return this.getAuthenticationManager().authenticate(authRequest);
    }

    // 부가 정보 셋팅용 헬퍼 메서드
    protected void setDetails(HttpServletRequest request, UsernamePasswordAuthenticationToken authRequest) {
        authRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));
    }
}



/*
        구분	        UsernamePasswordAuthenticationFilter	                                                        AbstractAuthenticationProcessingFilter
        기본 동작	application/x-www-form-urlencoded 형태의 Form 데이터(username=a&password=b)를 받도록 설계됨	        아무런 기본 동작이 없음. 백지 상태
        JSON 로그인	JSON을 읽으려면 어차피 내부 로직을 다 뜯어고쳐야 함 (Override)	                                        JSON 파싱 로직을 짜기에 가장 적합하고 깔끔함
        추천 상황	전통적인 SSR 방식 (JSP, Thymeleaf)의 <form> 태그 로그인	                                            React, Vue, 모바일 앱 등 REST API (JSON) 방식


 */