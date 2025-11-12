package com.ilovepc.project_home.web.auth.service;

import com.ilovepc.project_home.repository.AuthMasterMapper;
import com.ilovepc.project_home.web.auth.vo.signup.SignUpParam;
import com.ilovepc.project_home.web.auth.vo.signup.SignUpRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class SignUpService {
    private final AuthMasterMapper authMasterMapper;
    private final PasswordEncoder passwordEncoder;

    //회원가입
    @Transactional
    public void signUp(SignUpRequest signUpRequest) {
        SignUpParam signUpParam = signUpRequest.makeParam();

        //비밀번호 암호화
        String encPassword = passwordEncoder.encode(signUpRequest.getPlainPassword());
        signUpParam.setPassword(encPassword);

        int pResult = authMasterMapper.pSignUp(signUpParam);
        log.error("pResult:{}", pResult);
    }

}
