package com.maru.domain.employment.exception;

import com.maru.common.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum EmploymentErrorCode implements BaseErrorCode {

    // 조회/권한
    NOT_FOUND(HttpStatus.NOT_FOUND, "EMPLOYMENT_001", "승인 요청을 찾을 수 없습니다"),
    NOT_OWNER(HttpStatus.FORBIDDEN, "EMPLOYMENT_002", "도장 관장만 승인/거절할 수 있습니다"),
    NOT_REQUESTER(HttpStatus.FORBIDDEN, "EMPLOYMENT_003", "본인의 요청만 취소할 수 있습니다"),

    // 필수값 누락
    USER_REQUIRED(HttpStatus.BAD_REQUEST, "EMPLOYMENT_101", "user는 필수입니다"),
    TENANT_REQUIRED(HttpStatus.BAD_REQUEST, "EMPLOYMENT_102", "tenant는 필수입니다"),
    DOJANG_REQUIRED(HttpStatus.BAD_REQUEST, "EMPLOYMENT_103", "dojang은 필수입니다"),
    EMPLOYMENT_REQUIRED(HttpStatus.BAD_REQUEST, "EMPLOYMENT_104", "employment는 필수입니다"),
    STATUS_REQUIRED(HttpStatus.BAD_REQUEST, "EMPLOYMENT_105", "status는 필수입니다"),

    // 비즈니스 규칙
    ALREADY_EXISTS(HttpStatus.CONFLICT, "EMPLOYMENT_301", "이미 해당 도장에 승인 요청이 존재합니다"),
    NOT_PENDING(HttpStatus.BAD_REQUEST, "EMPLOYMENT_302", "대기 상태의 요청만 처리할 수 있습니다"),
    NOT_ACTIVE(HttpStatus.BAD_REQUEST, "EMPLOYMENT_303", "활성 상태의 고용만 정지할 수 있습니다"),
    NOT_SUSPENDED(HttpStatus.BAD_REQUEST, "EMPLOYMENT_304", "정지 상태의 고용만 재활성화할 수 있습니다"),
    NOT_ACTIVE_OR_SUSPENDED(HttpStatus.BAD_REQUEST, "EMPLOYMENT_305", "활성 또는 정지 상태의 고용만 퇴사 처리할 수 있습니다"),
    NOT_LEFT_OR_REJECTED(HttpStatus.BAD_REQUEST, "EMPLOYMENT_306", "퇴사 또는 거절 상태의 고용만 재입사 처리할 수 있습니다"),
    TENANT_MISMATCH(HttpStatus.BAD_REQUEST, "EMPLOYMENT_307", "도장의 Tenant와 입력된 Tenant가 일치하지 않습니다");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
