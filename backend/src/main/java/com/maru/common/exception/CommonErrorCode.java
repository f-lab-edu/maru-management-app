package com.maru.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements BaseErrorCode {

    // 조회 실패
    NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON_001", "요청한 리소스를 찾을 수 없습니다"),

    // 유효성 검증
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON_201", "잘못된 요청입니다"),

    // 서버 오류
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_501", "서버 내부 오류가 발생했습니다"),
    NOT_IMPLEMENTED(HttpStatus.NOT_IMPLEMENTED, "COMMON_502", "아직 구현되지 않은 기능입니다");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
