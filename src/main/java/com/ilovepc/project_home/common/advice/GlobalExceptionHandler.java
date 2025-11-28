package com.ilovepc.project_home.common.advice;

import com.ilovepc.project_home.common.exception.BusinessException;
import com.ilovepc.project_home.common.vo.ApiResponse;
import com.ilovepc.project_home.common.vo.ErrCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        ErrCode errCode = e.getErrCode();
        String message = errCode.getMessage();
        String code = errCode.getCode();
        HttpStatus httpStatus = errCode.getHttpStatus();
        ApiResponse.fail(code, message);
        return ResponseEntity
                .status(httpStatus) // 여기서 401, 403, 500 등이 설정
                .body(ApiResponse.fail(code, message)); //내가 원하는 Body
    }
}
