package com.maru.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SmsErrorCode implements BaseErrorCode {

    // 조회 실패
    CODE_NOT_FOUND(HttpStatus.BAD_REQUEST, "SMS_001", "인증 요청 내역이 없습니다"),

    // 유효성 검증
    CODE_INVALID(HttpStatus.BAD_REQUEST, "SMS_201", "인증번호가 일치하지 않습니다"),
    CODE_EXPIRED(HttpStatus.BAD_REQUEST, "SMS_202", "인증번호가 만료되었습니다"),

    // 비즈니스 규칙
    USER_MISMATCH(HttpStatus.FORBIDDEN, "SMS_301", "인증 요청자와 검증 요청자가 일치하지 않습니다"),
    RESEND_TOO_FAST(HttpStatus.TOO_MANY_REQUESTS, "SMS_302", "재발송 요청은 1분에 한번씩 할 수 있습니다"),
    MAX_ATTEMPTS_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "SMS_303", "인증 시도 횟수를 초과했습니다"),
    SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SMS_304", "SMS 발송에 실패했습니다");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
