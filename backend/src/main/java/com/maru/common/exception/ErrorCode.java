package com.maru.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {

    // 인증 관련 에러 (AUTH_XXX)
    AUTH_REQUIRED("AUTH_001", "인증이 필요합니다"),
    AUTH_INVALID_TOKEN("AUTH_002", "유효하지 않은 토큰입니다"),
    AUTH_ACCESS_DENIED("AUTH_003", "접근 권한이 없습니다"),
    AUTH_TOKEN_EXPIRED("AUTH_004", "토큰이 만료되었습니다"),
    AUTH_REFRESH_TOKEN_EXPIRED("AUTH_005", "리프레시 토큰이 만료되었습니다. 다시 로그인해주세요"),
    AUTH_REFRESH_TOKEN_INVALID("AUTH_006", "유효하지 않은 리프레시 토큰입니다"),
    AUTH_REFRESH_TOKEN_REQUIRED("AUTH_007", "리프레시 토큰이 필요합니다"),
    AUTH_OAUTH_FAILED("AUTH_008", "OAuth 인증에 실패했습니다"),
    AUTH_OAUTH_INVALID_CODE("AUTH_009", "유효하지 않은 인가 코드입니다"),
    AUTH_OAUTH_USER_INFO_FAILED("AUTH_010", "사용자 정보 조회에 실패했습니다"),

    // 사용자 관련 에러 (USER_XXX)
    USER_NOT_FOUND("USER_001", "사용자를 찾을 수 없습니다"),
    USER_INVALID_ROLE("USER_002", "유효하지 않은 역할입니다"),

    // 온보딩 관련 에러 (ONBOARDING_XXX)
    ONBOARDING_STAGE_INVALID("ONBOARDING_001", "현재 온보딩 단계에서 수행할 수 없는 작업입니다"),

    // 일반 에러 (COMMON_XXX)
    BAD_REQUEST("COMMON_001", "잘못된 요청입니다"),
    NOT_FOUND("COMMON_002", "요청한 리소스를 찾을 수 없습니다"),
    INTERNAL_SERVER_ERROR("COMMON_003", "서버 내부 오류가 발생했습니다");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
