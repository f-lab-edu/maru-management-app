package com.maru.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // 인증 관련 에러 (AUTH_XXX) - 401
    AUTH_REQUIRED(HttpStatus.UNAUTHORIZED, "AUTH_001", "인증이 필요합니다"),
    AUTH_INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_002", "유효하지 않은 토큰입니다"),
    AUTH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH_004", "토큰이 만료되었습니다"),
    AUTH_REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH_005", "리프레시 토큰이 만료되었습니다. 다시 로그인해주세요"),
    AUTH_REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "AUTH_006", "유효하지 않은 리프레시 토큰입니다"),
    AUTH_REFRESH_TOKEN_REQUIRED(HttpStatus.UNAUTHORIZED, "AUTH_007", "리프레시 토큰이 필요합니다"),
    AUTH_OAUTH_FAILED(HttpStatus.UNAUTHORIZED, "AUTH_008", "OAuth 인증에 실패했습니다"),
    AUTH_OAUTH_INVALID_CODE(HttpStatus.UNAUTHORIZED, "AUTH_009", "유효하지 않은 인가 코드입니다"),
    AUTH_OAUTH_USER_INFO_FAILED(HttpStatus.UNAUTHORIZED, "AUTH_010", "사용자 정보 조회에 실패했습니다"),

    // 인증 관련 에러 (AUTH_XXX) - 403
    AUTH_ACCESS_DENIED(HttpStatus.FORBIDDEN, "AUTH_003", "접근 권한이 없습니다"),

    // 사용자 관련 에러 (USER_XXX)
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_001", "사용자를 찾을 수 없습니다"),
    USER_INVALID_ROLE(HttpStatus.BAD_REQUEST, "USER_002", "유효하지 않은 역할입니다"),

    // 온보딩 관련 에러 (ONBOARDING_XXX)
    ONBOARDING_STAGE_INVALID(HttpStatus.BAD_REQUEST, "ONBOARDING_001", "현재 온보딩 단계에서 수행할 수 없는 작업입니다"),
    ONBOARDING_PHONE_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "ONBOARDING_002", "전화번호 인증이 완료되지 않았습니다"),

    // SMS 인증 관련 에러 (SMS_XXX)
    SMS_CODE_INVALID(HttpStatus.BAD_REQUEST, "SMS_001", "인증번호가 일치하지 않습니다"),
    SMS_CODE_EXPIRED(HttpStatus.BAD_REQUEST, "SMS_002", "인증번호가 만료되었습니다"),
    SMS_CODE_NOT_FOUND(HttpStatus.BAD_REQUEST, "SMS_003", "인증 요청 내역이 없습니다"),
    SMS_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SMS_004", "SMS 발송에 실패했습니다"),
    SMS_RESEND_TOO_FAST(HttpStatus.TOO_MANY_REQUESTS, "SMS_005", "재발송 요청은 1분에 한번씩 할 수 있습니다"),
    SMS_MAX_ATTEMPTS_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "SMS_006", "인증 시도 횟수를 초과했습니다"),
    SMS_USER_MISMATCH(HttpStatus.FORBIDDEN, "SMS_007", "인증 요청자와 검증 요청자가 일치하지 않습니다"),

    // 일반 에러 (COMMON_XXX)
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON_001", "잘못된 요청입니다"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON_002", "요청한 리소스를 찾을 수 없습니다"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_003", "서버 내부 오류가 발생했습니다"),
    NOT_IMPLEMENTED(HttpStatus.NOT_IMPLEMENTED, "COMMON_004", "아직 구현되지 않은 기능입니다");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
