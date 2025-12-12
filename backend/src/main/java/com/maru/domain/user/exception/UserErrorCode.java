package com.maru.domain.user.exception;

import com.maru.common.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements BaseErrorCode {

    // 조회 실패
    NOT_FOUND(HttpStatus.NOT_FOUND, "USER_001", "사용자를 찾을 수 없습니다"),

    // 필수값 누락
    USER_REQUIRED(HttpStatus.BAD_REQUEST, "USER_101", "user는 필수입니다"),
    NAME_REQUIRED(HttpStatus.BAD_REQUEST, "USER_102", "name은 필수입니다"),
    INITIAL_STEP_REQUIRED(HttpStatus.BAD_REQUEST, "USER_103", "initialStep은 필수입니다"),
    PROVIDER_REQUIRED(HttpStatus.BAD_REQUEST, "USER_104", "provider는 필수입니다"),
    PROVIDER_ACCOUNT_ID_REQUIRED(HttpStatus.BAD_REQUEST, "USER_105", "providerAccountId는 필수입니다"),

    // 유효성 검증
    INVALID_ROLE(HttpStatus.BAD_REQUEST, "USER_201", "유효하지 않은 역할입니다");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
