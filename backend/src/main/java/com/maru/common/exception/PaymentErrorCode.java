package com.maru.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PaymentErrorCode implements BaseErrorCode {

    // 비즈니스 규칙 위반
    AMOUNT_EXCEEDS_REMAINING(HttpStatus.BAD_REQUEST, "PAYMENT_301", "수납 금액이 남은 금액을 초과합니다"),
    INVALID_AMOUNT(HttpStatus.BAD_REQUEST, "PAYMENT_302", "유효하지 않은 수납 금액입니다");

    private final HttpStatus status;
    private final String code;
    private final String message;
}