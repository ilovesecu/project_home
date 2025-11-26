package com.ilovepc.project_home.common.vo;

import org.springframework.http.HttpStatus;

public interface ErrCode {
    HttpStatus getHttpStatus();
    String getCode();
    String getMessage();
}
