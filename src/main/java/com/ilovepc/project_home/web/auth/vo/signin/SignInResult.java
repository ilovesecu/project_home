package com.ilovepc.project_home.web.auth.vo.signin;

import lombok.Data;

@Data
public class SignInResult {
    private SignInRetValCode resultCode; // DB 컬럼 retVal에 맵핑
    private int userNo;
}
