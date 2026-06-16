package com.ilovepc.project_home.common.advice;

import com.ilovepc.project_home.common.exception.BusinessException;
import com.ilovepc.project_home.common.vo.ApiResponse;
import com.ilovepc.project_home.common.vo.ErrCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        ErrCode errCode = e.getErrCode();
        return ResponseEntity
                .status(errCode.getHttpStatus())
                .body(ApiResponse.fail(errCode.getCode(), errCode.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail("REQ_001", e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e
    ) {
        //String message = "요청 파라미터 형식이 올바르지 않습니다. parameter=" + e.getName();
        String message = "요청 파라미터 오류";
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail("REQ_002", message));
    }
}
