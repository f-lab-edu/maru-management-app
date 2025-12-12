package com.maru.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OnboardingErrorCode implements BaseErrorCode {

    // 비즈니스 규칙
    STAGE_INVALID(HttpStatus.BAD_REQUEST, "ONBOARDING_301", "현재 온보딩 단계에서 수행할 수 없는 작업입니다"),
    PHONE_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "ONBOARDING_302", "전화번호 인증이 완료되지 않았습니다");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
