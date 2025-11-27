package com.maru.common.exception;

import lombok.Getter;

@Getter
public class SmsVerificationException extends BusinessException {

    private final int remainingAttempts;

    public SmsVerificationException(ErrorCode errorCode, int remainingAttempts) {
        super(errorCode);
        this.remainingAttempts = remainingAttempts;
    }
}
