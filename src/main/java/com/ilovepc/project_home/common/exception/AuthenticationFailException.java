package com.ilovepc.project_home.common.exception;

import com.ilovepc.project_home.common.vo.CommonErrCode;
import com.ilovepc.project_home.common.vo.ErrCode;
import lombok.Getter;
import org.springframework.security.core.AuthenticationException;

@Getter
public class AuthenticationFailException extends AuthenticationException {
    private final ErrCode errCode;

    public AuthenticationFailException(ErrCode errCode) {
        super(errCode.getMessage());
        this.errCode = errCode;
    }
}
