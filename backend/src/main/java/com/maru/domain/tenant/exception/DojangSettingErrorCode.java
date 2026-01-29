package com.maru.domain.tenant.exception;

import com.maru.common.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum DojangSettingErrorCode implements BaseErrorCode {

    NOT_FOUND(HttpStatus.NOT_FOUND, "DOJANG_SETTING_001", "도장 설정을 찾을 수 없습니다"),
    DOJANG_REQUIRED(HttpStatus.BAD_REQUEST, "DOJANG_SETTING_101", "도장 ID는 필수입니다");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
