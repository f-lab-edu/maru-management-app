package com.maru.domain.tenant.exception;

import com.maru.common.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TenantErrorCode implements BaseErrorCode {

    // 조회 실패
    NOT_FOUND(HttpStatus.NOT_FOUND, "TENANT_001", "테넌트를 찾을 수 없습니다"),

    // 필수값 누락
    OWNER_REQUIRED(HttpStatus.BAD_REQUEST, "TENANT_101", "owner는 필수입니다"),
    SLUG_REQUIRED(HttpStatus.BAD_REQUEST, "TENANT_102", "slug는 필수입니다");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
