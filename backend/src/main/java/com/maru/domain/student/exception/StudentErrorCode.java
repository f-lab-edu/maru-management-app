package com.maru.domain.student.exception;

import com.maru.common.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum StudentErrorCode implements BaseErrorCode {

    // 조회 실패
    NOT_FOUND(HttpStatus.NOT_FOUND, "STUDENT_001", "원생을 찾을 수 없습니다"),

    // 필수값 누락
    DOJANG_REQUIRED(HttpStatus.BAD_REQUEST, "STUDENT_101", "dojang은 필수입니다"),
    NAME_REQUIRED(HttpStatus.BAD_REQUEST, "STUDENT_102", "name은 필수입니다"),
    BIRTH_REQUIRED(HttpStatus.BAD_REQUEST, "STUDENT_103", "birth는 필수입니다"),

    // 비즈니스 규칙
    DUPLICATE(HttpStatus.CONFLICT, "STUDENT_301", "이미 등록된 원생입니다");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
