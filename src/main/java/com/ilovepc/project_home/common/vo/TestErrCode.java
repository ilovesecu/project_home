package com.ilovepc.project_home.common.vo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum TestErrCode implements ErrCode{
    TEST_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SYS_002", "알 수 없는 에러");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
