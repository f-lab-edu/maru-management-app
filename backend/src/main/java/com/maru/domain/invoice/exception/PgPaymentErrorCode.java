package com.maru.domain.invoice.exception;

import com.maru.common.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PgPaymentErrorCode implements BaseErrorCode {

    PG_DETAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "PG_PAYMENT_001", "PG 결제 정보를 찾을 수 없습니다"),
    PAYMENT_LINK_NOT_FOUND(HttpStatus.NOT_FOUND, "PG_PAYMENT_002", "결제 링크를 찾을 수 없습니다"),

    PAYMENT_LINK_EXPIRED(HttpStatus.BAD_REQUEST, "PG_PAYMENT_301", "결제 링크가 만료되었습니다"),
    PAYMENT_LINK_ALREADY_USED(HttpStatus.BAD_REQUEST, "PG_PAYMENT_302", "이미 사용된 결제 링크입니다"),
    AMOUNT_EXCEEDS_REMAINING(HttpStatus.BAD_REQUEST, "PG_PAYMENT_303", "결제 금액이 남은 금액을 초과합니다"),
    INVOICE_ALREADY_PAID(HttpStatus.BAD_REQUEST, "PG_PAYMENT_304", "이미 완납된 청구서입니다"),
    AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "PG_PAYMENT_305", "결제 금액이 일치하지 않습니다"),

    PAYMENT_IN_PROGRESS(HttpStatus.CONFLICT, "PG_PAYMENT_306", "결제가 진행 중입니다"),
    PAYMENT_REQUIRES_MANUAL_CHECK(HttpStatus.CONFLICT, "PG_PAYMENT_307", "결제 처리 중 오류가 발생했습니다. 운영자 확인이 필요합니다"),

    TOSS_CONFIRM_FAILED(HttpStatus.BAD_GATEWAY, "PG_PAYMENT_501", "결제 승인에 실패했습니다"),
    TOSS_CANCEL_FAILED(HttpStatus.BAD_GATEWAY, "PG_PAYMENT_502", "결제 취소에 실패했습니다"),
    COMPENSATION_CANCEL_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "PG_PAYMENT_503", "보상 취소에 실패했습니다");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
