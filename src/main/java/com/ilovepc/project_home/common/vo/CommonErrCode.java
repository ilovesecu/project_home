package com.ilovepc.project_home.common.vo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommonErrCode {
    //회원가입 & 로그인 관련
    USER_NOT_FOUND(HttpStatus.UNAUTHORIZED, "AUTH_001", "아이디 혹은 비밀번호가 잘못되었습니다."),
    BAD_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH_002", "아이디 혹은 비밀번호가 잘못되었습니다."),
    ACCOUNT_LOCKED(HttpStatus.UNAUTHORIZED, "AUTH_003", "계정이 잠겼습니다. 관리자에게 문의하세요."),
    ACCOUNT_SUSPENDED(HttpStatus.FORBIDDEN, "AUTH_004", "이용이 정지된 계정입니다."),
    PASSWORD_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH_005", "비밀번호 유효기간이 만료되었습니다."),

    //사용자 관련
    IP_SUSPENDED(HttpStatus.UNAUTHORIZED, "USER_001", "정지된 IP 입니다."),


    // 시스템 관련
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SYS_001", "서버 내부 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
