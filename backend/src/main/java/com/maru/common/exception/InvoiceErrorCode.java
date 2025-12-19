package com.maru.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum InvoiceErrorCode implements BaseErrorCode {

    // 리소스 조회 실패
    NOT_FOUND(HttpStatus.NOT_FOUND, "INVOICE_001", "청구서를 찾을 수 없습니다"),

    // 필수값 누락
    STUDENT_REQUIRED(HttpStatus.BAD_REQUEST, "INVOICE_101", "원생 정보는 필수입니다"),
    DUE_DATE_REQUIRED(HttpStatus.BAD_REQUEST, "INVOICE_102", "납부 마감일은 필수입니다"),
    AMOUNT_REQUIRED(HttpStatus.BAD_REQUEST, "INVOICE_103", "청구 금액은 필수입니다"),

    // 비즈니스 규칙 위반
    DUPLICATE_INVOICE(HttpStatus.CONFLICT, "INVOICE_301", "해당 월에 이미 청구서가 존재합니다"),
    INVALID_STATUS_TRANSITION(HttpStatus.BAD_REQUEST, "INVOICE_302", "잘못된 상태 변경입니다"),
    CANNOT_VOID_PAID_INVOICE(HttpStatus.BAD_REQUEST, "INVOICE_303", "완납된 청구서는 무효화할 수 없습니다"),
    CANNOT_UPDATE_NON_DRAFT(HttpStatus.BAD_REQUEST, "INVOICE_304", "초안 상태의 청구서만 수정할 수 있습니다");

    private final HttpStatus status;
    private final String code;
    private final String message;
}