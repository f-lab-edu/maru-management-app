package com.maru.common.exception;

import lombok.Getter;
import org.springframework.security.core.AuthenticationException;

@Getter
public class AuthException extends AuthenticationException {

    private final ErrorCode errorCode;

    public AuthException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public AuthException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }
}
