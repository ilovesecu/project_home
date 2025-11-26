package com.ilovepc.project_home.common.exception;

import lombok.Getter;
import org.springframework.security.core.AuthenticationException;

@Getter
public class AuthenticationFailException extends AuthenticationException {
    private final String errorCode;

    public AuthenticationFailException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}
