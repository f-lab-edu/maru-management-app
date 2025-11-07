package com.maru.common.exception;

import lombok.Getter;
import org.springframework.security.core.AuthenticationException;

/**
 * 인증 관련 예외
 * ErrorCode를 포함하여 메시지 중복을 제거
 */
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
