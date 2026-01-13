package com.ilovepc.project_home.common.vo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommonErrCode implements ErrCode{
    //회원가입 & 로그인 관련
    USER_NOT_FOUND(HttpStatus.UNAUTHORIZED, "AUTH_001", "아이디 혹은 비밀번호가 잘못되었습니다."),
    BAD_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH_002", "아이디 혹은 비밀번호가 잘못되었습니다."),
    ACCOUNT_LOCKED(HttpStatus.UNAUTHORIZED, "AUTH_003", "계정이 잠겼습니다. 관리자에게 문의하세요."),
    ACCOUNT_SUSPENDED(HttpStatus.FORBIDDEN, "AUTH_004", "이용이 정지된 계정입니다."),
    PASSWORD_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH_005", "비밀번호 유효기간이 만료되었습니다."),
    //토큰 관련
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "TOKEN_001", "토큰이 만료되었습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "TOKEN_002", "유효하지 않는 토큰입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "TOKEN_003", "인증이 필요합니다. 정확한 토큰을 입력해주세요."),

    //권한 관련
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "ROLE_001", "권한이 부족합니다."),

    //사용자 관련
    IP_SUSPENDED(HttpStatus.UNAUTHORIZED, "USER_001", "정지된 IP 입니다."),
    USER_SUSPENDED(HttpStatus.UNAUTHORIZED, "USER_002", "휴면회원입니다."),
    USER_INACTIVE(HttpStatus.UNAUTHORIZED, "USER_003", "휴면회원입니다."),

    // 시스템 관련
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SYS_001", "서버 내부 오류가 발생했습니다."),
    UNKNOWN_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SYS_002", "알 수 없는 에러");


    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    public static CommonErrCode fromCode(String code){
        for(CommonErrCode errCode : CommonErrCode.values()){
            if(errCode.getCode().equals(code)){
                return errCode;
            }
        }
        return null;
    }
}
