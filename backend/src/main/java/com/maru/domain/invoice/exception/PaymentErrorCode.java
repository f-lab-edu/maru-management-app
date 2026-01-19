package com.maru.domain.invoice.exception;

import com.maru.common.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PaymentErrorCode implements BaseErrorCode {

    // 리소스 조회 실패
    NOT_FOUND(HttpStatus.NOT_FOUND, "PAYMENT_001", "수납 내역을 찾을 수 없습니다"),

    // 필수값 누락
    INVOICE_REQUIRED(HttpStatus.BAD_REQUEST, "PAYMENT_101", "청구서 정보가 필요합니다"),
    AMOUNT_REQUIRED(HttpStatus.BAD_REQUEST, "PAYMENT_102", "수납 금액이 필요합니다"),

    // 비즈니스 규칙 위반
    AMOUNT_EXCEEDS_REMAINING(HttpStatus.BAD_REQUEST, "PAYMENT_301", "수납 금액이 남은 금액을 초과합니다"),
    INVALID_AMOUNT(HttpStatus.BAD_REQUEST, "PAYMENT_302", "유효하지 않은 수납 금액입니다"),
    ALREADY_REFUNDED(HttpStatus.BAD_REQUEST, "PAYMENT_303", "이미 환불된 수납입니다");

    private final HttpStatus status;
    private final String code;
    private final String message;
}