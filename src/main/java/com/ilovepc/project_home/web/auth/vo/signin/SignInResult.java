package com.ilovepc.project_home.web.auth.vo.signin;

import lombok.Data;

@Data
public class SignInResult {
    private String email;
    private String password;
    private String nickname;
    private int userNo;
}
