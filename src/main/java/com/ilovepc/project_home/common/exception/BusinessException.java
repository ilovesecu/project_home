package com.ilovepc.project_home.common.exception;

import com.ilovepc.project_home.common.vo.ErrCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 전역 예외 처리기 (GlobalExceptionHandler) 에서 처리하는 Service Layer Exception 입니다.
 */

@Getter
@RequiredArgsConstructor
public class BusinessException extends RuntimeException{
    private final ErrCode errCode;
}
