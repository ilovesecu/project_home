package com.ilovepc.project_home.common.vo;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ApiResponse<T> {
    private String status; // "SUCCESS" or "FAIL"
    private String message; // Client에게 보여줄 메시지
    private String code; //에러코드 (AUTH_001, LOGIN_FAIL_PWD 등), 성공코드 (200, 201 등등)
    private T data;

    //성공 응답
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(
                "SUCCESS",
                "요청이 성공적으로 처리되었습니다.",
                "200",
                data
        );
    }
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(
                "SUCCESS",
                "요청이 성공적으로 처리되었습니다.",
                "200",
                null
        );
    }

    //실패응답
    public static <T> ApiResponse<T> fail(String code, String message) {
        return new ApiResponse<>(
                "FAIL",
                message,
                code,
                null
        );
    }
}
