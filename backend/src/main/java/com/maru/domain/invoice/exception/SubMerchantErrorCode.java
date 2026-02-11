package com.maru.domain.invoice.exception;

import com.maru.common.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SubMerchantErrorCode implements BaseErrorCode {

    NOT_FOUND(HttpStatus.NOT_FOUND, "SUB_MERCHANT_001", "서브몰 정보를 찾을 수 없습니다"),

    ALREADY_REGISTERED(HttpStatus.CONFLICT, "SUB_MERCHANT_301", "이미 등록된 서브몰입니다"),
    INVALID_STATUS_TRANSITION(HttpStatus.BAD_REQUEST, "SUB_MERCHANT_302", "유효하지 않은 상태 변경입니다"),
    NOT_ACTIVE(HttpStatus.BAD_REQUEST, "SUB_MERCHANT_303", "서브몰이 활성화되지 않았습니다");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
