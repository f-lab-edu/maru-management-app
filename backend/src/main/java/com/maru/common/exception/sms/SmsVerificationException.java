package com.maru.common.exception.sms;

import com.maru.common.exception.BaseErrorCode;
import com.maru.common.exception.BusinessException;
import lombok.Getter;

@Getter
public class SmsVerificationException extends BusinessException {

    private final int remainingAttempts;

    public SmsVerificationException(BaseErrorCode errorCode, int remainingAttempts) {
        super(errorCode);
        this.remainingAttempts = remainingAttempts;
    }
}
