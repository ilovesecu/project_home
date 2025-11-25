package com.ilovepc.project_home.config.security.authentication;

import com.ilovepc.project_home.config.security.HomeProjectUserDetailsService;
import com.ilovepc.project_home.config.security.vo.HomeProjectUserDetails;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;


/*
Provider는 유효성을 판단하는 역할까지만 수행합니다.
Provider가 성공적으로 인증 객체(Authentication)를 반환하면,
토큰 발급은 다음 단계인 **AuthenticationSuccessHandler**의 역할입니다.
 */
public class HomeProjectAuthenticationProvider implements AuthenticationProvider {
    private final HomeProjectUserDetailsService homeProjectUserDetailsService;
    private final PasswordEncoder passwordEncoder;

    public HomeProjectAuthenticationProvider(HomeProjectUserDetailsService homeProjectUserDetailsService,
                                             PasswordEncoder passwordEncoder) {
        this.homeProjectUserDetailsService = homeProjectUserDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        //역할 : (핵심!) 실제 DB에서 유저를 가져오고(loadUserByUsername) 검증하여 인증 도장(Authentication 객체)을 찍어줍니다.
        //1. 미인증 토큰에서 ID/PW 추출
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        //2. UserDetailsSerivce를 통해 DB에서 사용자 정보 GET!
        HomeProjectUserDetails userDetails = (HomeProjectUserDetails) homeProjectUserDetailsService.loadUserByUsername(username);

        //3. 비번 검증
        if(!passwordEncoder.matches(password, userDetails.getPassword())){
            throw new BadCredentialsException("Invalid username or password");
        }

        //4. 통과 시 인증된 Authentication 객체반환
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        //"네, 제가 UsernamePassword 토큰 처리할 수 있습니다. --> Form Login, 또는 JSON 로그인 필터(AbstractAuthenticationProcessingFilter)가 생성하는 기본 토큰 타입입니다."
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
