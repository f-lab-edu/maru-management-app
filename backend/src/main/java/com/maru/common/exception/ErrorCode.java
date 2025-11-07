package com.maru.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {

    // 인증 관련 에러 (AUTH_XXX)
    AUTH_REQUIRED("AUTH_001", "인증이 필요합니다"),
    AUTH_INVALID_TOKEN("AUTH_002", "유효하지 않은 토큰입니다"),
    AUTH_ACCESS_DENIED("AUTH_003", "접근 권한이 없습니다"),
    AUTH_TOKEN_EXPIRED("AUTH_004", "토큰이 만료되었습니다");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
