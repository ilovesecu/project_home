package com.ilovepc.project_home.web.auth.vo.signin;

import lombok.Getter;

@Getter
public enum SignInRetValCode {
    SUCCESS("10000", "정상 로그인 가능"),
    ID_NOT_FOUND("59001", "아이디 없음"),
    INACTIVE_USER("59002", "장기 미이용 회원"),
    IP_BLOCK("59004", "IP 정지"),
    REST_USER("59005", "휴면 설정 회원"),
    UNKNOWN_ERROR(null, "알 수 없는 오류"); // 폴백(Fallback) 처리

    private final String code;
    private final String message;

    SignInRetValCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    /** 데이터베이스 코드(String)를 Enum으로 변환*/
    public static SignInRetValCode fromCode(String code) {
        if (code == null) {
            return UNKNOWN_ERROR;
        }
        for (SignInRetValCode result : SignInRetValCode.values()) {
            // DB에서 넘어온 문자열 코드와 Enum의 코드를 비교
            if (code.equals(result.code)) {
                return result;
            }
        }
        return UNKNOWN_ERROR;
    }
}
