package com.ilovepc.project_home.config.security;

import com.ilovepc.project_home.config.security.vo.HomeProjectUserDetails;
import com.ilovepc.project_home.config.security.vo.User;
import com.ilovepc.project_home.repository.AuthMasterMapper;
import com.ilovepc.project_home.utils.ClientUtils;
import com.ilovepc.project_home.web.auth.vo.signin.SignInParam;
import com.ilovepc.project_home.web.auth.vo.signin.SignInResult;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class HomeProjectUserDetailsService implements UserDetailsService {
    private final AuthMasterMapper authMasterMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. 현재 HTTP 요청 객체 가져오기
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        // 2. IP 주소 추출
        String clientIp = ClientUtils.getClientIp(request);

        SignInResult signInResult = authMasterMapper.pSignIn(SignInParam.builder()
                    .email(username)
                    .memIp(clientIp)
                    .build());

        //User user = new User(signInResult.getEmail(), signInResult.getPassword(), new ArrayList<>());
        //return new HomeProjectUserDetails(user);
        return null;
    }

    private UserDetails createUserDetails(SignInResult signInResult) {
        return null;
    }
}
