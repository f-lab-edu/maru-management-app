package com.maru.common.exception.auth;

import com.maru.common.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements BaseErrorCode {

    // 인증/권한 실패
    REQUIRED(HttpStatus.UNAUTHORIZED, "AUTH_001", "인증이 필요합니다"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "AUTH_002", "접근 권한이 없습니다"),

    // 필수값 누락
    REFRESH_TOKEN_REQUIRED(HttpStatus.UNAUTHORIZED, "AUTH_101", "리프레시 토큰이 필요합니다"),

    // 토큰 유효성 검증
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_201", "유효하지 않은 토큰입니다"),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH_202", "토큰이 만료되었습니다"),
    REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "AUTH_203", "유효하지 않은 리프레시 토큰입니다"),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH_204", "리프레시 토큰이 만료되었습니다. 다시 로그인해주세요"),

    // OAuth 관련
    OAUTH_FAILED(HttpStatus.UNAUTHORIZED, "AUTH_301", "OAuth 인증에 실패했습니다"),
    OAUTH_INVALID_CODE(HttpStatus.UNAUTHORIZED, "AUTH_302", "유효하지 않은 인가 코드입니다"),
    OAUTH_USER_INFO_FAILED(HttpStatus.UNAUTHORIZED, "AUTH_303", "사용자 정보 조회에 실패했습니다"),

    // 데모 관련
    DEMO_NOT_CONFIGURED(HttpStatus.SERVICE_UNAVAILABLE, "AUTH_401", "데모 로그인이 설정되지 않았습니다"),
    NO_EMPLOYMENT(HttpStatus.FORBIDDEN, "AUTH_402", "소속된 도장이 없습니다"),
    DEMO_RESTRICTED(HttpStatus.FORBIDDEN, "AUTH_403", "데모 계정에서는 이 기능을 사용할 수 없습니다");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
