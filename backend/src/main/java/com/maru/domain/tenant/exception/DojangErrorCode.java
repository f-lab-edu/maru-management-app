package com.maru.domain.tenant.exception;

import com.maru.common.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum DojangErrorCode implements BaseErrorCode {

    // 조회/권한
    NOT_FOUND(HttpStatus.NOT_FOUND, "DOJANG_001", "도장을 찾을 수 없습니다"),
    UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "DOJANG_002", "해당 도장에 접근 권한이 없습니다"),

    // 필수값 누락
    TENANT_REQUIRED(HttpStatus.BAD_REQUEST, "DOJANG_101", "tenant는 필수입니다"),
    OWNER_REQUIRED(HttpStatus.BAD_REQUEST, "DOJANG_102", "owner는 필수입니다"),
    NAME_REQUIRED(HttpStatus.BAD_REQUEST, "DOJANG_103", "name은 필수입니다"),

    // 비즈니스 규칙
    OWNER_TENANT_MISMATCH(HttpStatus.BAD_REQUEST, "DOJANG_301", "도장 소유자는 테넌트 소유자와 일치해야 합니다");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
