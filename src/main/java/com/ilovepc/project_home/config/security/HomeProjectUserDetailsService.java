package com.ilovepc.project_home.config.security;

import com.ilovepc.project_home.config.security.vo.HomeProjectUserDetails;
import com.ilovepc.project_home.config.security.vo.User;
import com.ilovepc.project_home.repository.AuthMasterMapper;
import com.ilovepc.project_home.web.auth.vo.signin.SignInParam;
import com.ilovepc.project_home.web.auth.vo.signin.SignInResult;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class HomeProjectUserDetailsService implements UserDetailsService {
    private final AuthMasterMapper authMasterMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SignInResult signInResult = authMasterMapper.pSignIn(SignInParam.builder().email(username).build());

        User user = new User(signInResult.getEmail(), signInResult.getPassword(), new ArrayList<>());
        return new HomeProjectUserDetails(user);
    }

    private UserDetails createUserDetails(SignInResult signInResult) {

    }
}
