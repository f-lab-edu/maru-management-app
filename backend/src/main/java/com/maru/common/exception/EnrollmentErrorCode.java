package com.maru.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum EnrollmentErrorCode implements BaseErrorCode {

    // 리소스 조회 실패
    NOT_ENROLLED(HttpStatus.NOT_FOUND, "ENROLLMENT_001", "등록되지 않은 원생입니다"),

    // 필수값 누락
    DOJANG_REQUIRED(HttpStatus.BAD_REQUEST, "ENROLLMENT_100", "도장 정보는 필수입니다"),
    DIVISION_REQUIRED(HttpStatus.BAD_REQUEST, "ENROLLMENT_101", "수련반 정보는 필수입니다"),
    STUDENT_REQUIRED(HttpStatus.BAD_REQUEST, "ENROLLMENT_102", "원생 정보는 필수입니다"),

    // 비즈니스 규칙 위반
    ALREADY_ENROLLED(HttpStatus.CONFLICT, "ENROLLMENT_301", "이미 등록된 원생입니다");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
