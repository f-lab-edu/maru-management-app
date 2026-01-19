package com.maru.domain.guardian.exception;

import com.maru.common.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum GuardianErrorCode implements BaseErrorCode {

    // 조회 실패
    NOT_FOUND(HttpStatus.NOT_FOUND, "GUARDIAN_001", "보호자를 찾을 수 없습니다"),

    // 필수값 누락
    PHONE_REQUIRED(HttpStatus.BAD_REQUEST, "GUARDIAN_101", "phone은 필수입니다"),
    NAME_REQUIRED(HttpStatus.BAD_REQUEST, "GUARDIAN_102", "name은 필수입니다"),
    GUARDIAN_REQUIRED(HttpStatus.BAD_REQUEST, "GUARDIAN_103", "guardian은 필수입니다"),
    STUDENT_REQUIRED(HttpStatus.BAD_REQUEST, "GUARDIAN_104", "student는 필수입니다"),

    // 비즈니스 규칙
    GUARDIANSHIP_ALREADY_EXISTS(HttpStatus.CONFLICT, "GUARDIAN_301", "이미 연결된 보호자입니다");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
