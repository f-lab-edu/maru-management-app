package com.maru.domain.section.exception;

import com.maru.common.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum DivisionErrorCode implements BaseErrorCode {

    NOT_FOUND(HttpStatus.NOT_FOUND, "DIVISION_001", "수련반을 찾을 수 없습니다"),

    DOJANG_REQUIRED(HttpStatus.BAD_REQUEST, "DIVISION_100", "도장 정보는 필수입니다"),
    SECTION_REQUIRED(HttpStatus.BAD_REQUEST, "DIVISION_101", "수련부 선택은 필수입니다"),
    NAME_REQUIRED(HttpStatus.BAD_REQUEST, "DIVISION_102", "수련반 이름은 필수입니다"),

    DUPLICATE_NAME(HttpStatus.CONFLICT, "DIVISION_301", "이미 존재하는 수련반 이름입니다"),

    DUPLICATE_ID_IN_REQUEST(HttpStatus.BAD_REQUEST, "DIVISION_401", "중복된 수련반 ID가 포함되어 있습니다"),
    REORDER_COUNT_MISMATCH(HttpStatus.BAD_REQUEST, "DIVISION_402", "요청한 수련반 개수가 실제 개수와 일치하지 않습니다");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
