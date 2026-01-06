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
    TENANT_REQUIRED(HttpStatus.BAD_REQUEST, "INVOICE_100", "테넌트 정보는 필수입니다"),
    STUDENT_REQUIRED(HttpStatus.BAD_REQUEST, "INVOICE_101", "원생 정보는 필수입니다"),
    DUE_DATE_REQUIRED(HttpStatus.BAD_REQUEST, "INVOICE_102", "납부 마감일은 필수입니다"),
    AMOUNT_REQUIRED(HttpStatus.BAD_REQUEST, "INVOICE_103", "청구 금액은 필수입니다"),
    BILLING_YEAR_MONTH_REQUIRED(HttpStatus.BAD_REQUEST, "INVOICE_104", "청구 연월은 필수입니다"),
    DOJANG_REQUIRED(HttpStatus.BAD_REQUEST, "INVOICE_105", "도장 정보는 필수입니다"),

    // 비즈니스 규칙 위반
    DUPLICATE_INVOICE(HttpStatus.CONFLICT, "INVOICE_301", "해당 월에 이미 청구서가 존재합니다"),
    PREPAID_DUPLICATE_INVOICE(HttpStatus.CONFLICT, "INVOICE_311", "선납 기간 내 청구서가 이미 존재합니다. 기존 청구서를 삭제하고 진행해주세요"),
    INVALID_STATUS_TRANSITION(HttpStatus.BAD_REQUEST, "INVOICE_302", "잘못된 상태 변경입니다"),
    CANNOT_VOID_PAID_INVOICE(HttpStatus.BAD_REQUEST, "INVOICE_303", "완납된 청구서는 무효화할 수 없습니다"),
    CANNOT_UPDATE_ISSUED_INVOICE(HttpStatus.BAD_REQUEST, "INVOICE_304", "수납 전 상태의 청구서만 수정할 수 있습니다"),
    CANNOT_UPDATE_WITH_PAYMENT(HttpStatus.BAD_REQUEST, "INVOICE_310", "수납 기록이 있는 청구서는 수정할 수 없습니다"),
    CANNOT_RESTORE_NON_VOID(HttpStatus.BAD_REQUEST, "INVOICE_305", "VOID 상태의 청구서만 복구할 수 있습니다"),
    CANNOT_DELETE_NON_DRAFT(HttpStatus.BAD_REQUEST, "INVOICE_306", "DRAFT 상태의 청구서만 삭제할 수 있습니다"),
    CANNOT_PAY_VOID_INVOICE(HttpStatus.BAD_REQUEST, "INVOICE_307", "무효화된 청구서에는 결제할 수 없습니다"),
    CANNOT_PAY_DRAFT_INVOICE(HttpStatus.BAD_REQUEST, "INVOICE_308", "초안 상태의 청구서에는 결제할 수 없습니다"),
    CANNOT_REFUND_VOID_INVOICE(HttpStatus.BAD_REQUEST, "INVOICE_309", "무효화된 청구서에는 환불할 수 없습니다");

    private final HttpStatus status;
    private final String code;
    private final String message;
}