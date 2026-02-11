package com.maru.domain.invoice.exception;

import com.maru.common.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum RefundErrorCode implements BaseErrorCode {

    REFUND_NOT_FOUND(HttpStatus.NOT_FOUND, "REFUND_001", "환불 내역을 찾을 수 없습니다"),

    AMOUNT_MUST_BE_POSITIVE(HttpStatus.BAD_REQUEST, "REFUND_301", "환불 금액은 0보다 커야 합니다"),
    AMOUNT_EXCEEDS_PAYMENT(HttpStatus.BAD_REQUEST, "REFUND_302", "환불 금액이 결제 금액을 초과합니다"),
    ALREADY_REFUNDED(HttpStatus.BAD_REQUEST, "REFUND_303", "이미 환불된 결제입니다"),

    TOSS_CANCEL_FAILED(HttpStatus.BAD_GATEWAY, "REFUND_501", "PG 환불 요청에 실패했습니다");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
