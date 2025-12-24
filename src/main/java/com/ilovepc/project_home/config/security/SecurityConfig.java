package com.ilovepc.project_home.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilovepc.project_home.config.security.authentication.HomeProjectAuthenticationFailureHandler;
import com.ilovepc.project_home.config.security.authentication.HomeProjectAuthenticationProvider;
import com.ilovepc.project_home.config.security.authentication.HomeProjectAuthenticationSuccessHandler;
import com.ilovepc.project_home.config.security.filter.HomeProjectAuthenticationFilter;
import com.ilovepc.project_home.config.security.filter.JwtAuthenticationFilter;
import com.ilovepc.project_home.config.security.vo.Role;
import com.ilovepc.project_home.jwt.service.RefreshTokenService;
import com.ilovepc.project_home.jwt.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
//@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final HomeProjectUserDetailsService homeProjectUserDetailsService;
    private final RefreshTokenService refreshTokenService;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    String[] publicUrls = {
            "/api/auth/**",
            "/api/wakeUp/**",
            "/api/holiday/month",
            "/api/holiday/today",
            "/api/todo/**"
    };

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        //1. Custom Provider 생성 및 DI
        HomeProjectAuthenticationProvider provider = new HomeProjectAuthenticationProvider(homeProjectUserDetailsService, passwordEncoder());
        
        //2. ProviderManager(AuthenticationManager 구현체)에 Provider 등록
        ProviderManager providerManager = new ProviderManager(List.of(provider));
        //3. 인증 성공 후 비밀번호 메모리에서 삭제
        providerManager.setEraseCredentialsAfterAuthentication(true);
        return providerManager;

        //return authenticationConfiguration.getAuthenticationManager(); //기존 기본 Manager
    }


    public HomeProjectAuthenticationFilter  homeProjectAuthenticationFilter() throws Exception {
        HomeProjectAuthenticationFilter filter = new HomeProjectAuthenticationFilter(objectMapper);
        // Manager 주입
        filter.setAuthenticationManager(authenticationManager());
        //Handler 주입
        filter.setAuthenticationSuccessHandler(new HomeProjectAuthenticationSuccessHandler(jwtUtil, refreshTokenService, objectMapper));
        filter.setAuthenticationFailureHandler(new HomeProjectAuthenticationFailureHandler(objectMapper));

        // 필요에 따라 SecurityContextRepository도 설정합니다.
        // filter.setSecurityContextRepository(contextRepository);
        return filter;
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // CSRF, Form-Login, HttpBasic 비활성화
        http.csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable);

        //세션 STATE_LESS로 변경 (JWT 사용)
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        //커스텀 JWT 필터 추가
        //JwtFilter를 UsernamePasswordAuthenticationFilter앞에 추가
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterAt(homeProjectAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        //경로별 인가(Authorization) 설정
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(publicUrls).permitAll()
                // /api/guest/** 경로는 GUEST, USER, ADMIN 모두 접근 가능
                .requestMatchers("/api/guest/**").hasAnyRole(
                        Role.ROLE_GUEST.name().replace("ROLE_", ""),
                        Role.ROLE_USER.name().replace("ROLE_", ""),
                        Role.ROLE_ADMIN.name().replace("ROLE_", "")
                )

                // /api/user/** 경로는 USER, ADMIN 접근 가능
                .requestMatchers("/api/user/**").hasAnyRole(
                        Role.ROLE_USER.name().replace("ROLE_", ""),
                        Role.ROLE_ADMIN.name().replace("ROLE_", "")
                )

                // /api/admin/** 경로는 ADMIN만 접근 가능
                .requestMatchers("/api/admin/**").hasRole(
                        Role.ROLE_ADMIN.name().replace("ROLE_", "")
                )

                // 그 외 모든 요청은 인증 필요
                .anyRequest().authenticated()
        );

        // (추가) 커스텀 예외 처리 핸들러 (선택)
        // http.exceptionHandling(ex -> ex
        //         .authenticationEntryPoint(new JwtAuthenticationEntryPoint()) // 401
        //         .accessDeniedHandler(new JwtAccessDeniedHandler())        // 403
        // );
        return http.build();
    }
}
