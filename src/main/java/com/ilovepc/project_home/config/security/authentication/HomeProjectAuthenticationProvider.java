package com.ilovepc.project_home.config.security.authentication;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

/*
Provider는 유효성을 판단하는 역할까지만 수행합니다.
Provider가 성공적으로 인증 객체(Authentication)를 반환하면,
토큰 발급은 다음 단계인 **AuthenticationSuccessHandler**의 역할입니다.
 */
public class HomeProjectAuthenticationProvider implements AuthenticationProvider {
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        return null;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
