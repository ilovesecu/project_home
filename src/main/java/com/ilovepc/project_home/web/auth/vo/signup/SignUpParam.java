package com.ilovepc.project_home.web.auth.vo.signup;

import lombok.Builder;

@Builder
public class SignUpParam {
    private String email;
    private String password;
    private String nickname;

    public void setPassword(String plainPassword) {
        this.password = plainPassword;
    }
}
