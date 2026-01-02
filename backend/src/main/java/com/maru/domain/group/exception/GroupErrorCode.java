package com.maru.domain.group.exception;

import com.maru.common.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum GroupErrorCode implements BaseErrorCode {

    // 조회 실패
    NOT_FOUND(HttpStatus.NOT_FOUND, "GROUP_001", "수련반을 찾을 수 없습니다"),

    // 필수값 누락
    SECTION_REQUIRED(HttpStatus.BAD_REQUEST, "GROUP_101", "수련부 선택은 필수입니다"),
    NAME_REQUIRED(HttpStatus.BAD_REQUEST, "GROUP_102", "수련반 이름은 필수입니다"),

    // 비즈니스 규칙
    DUPLICATE_NAME(HttpStatus.CONFLICT, "GROUP_301", "이미 존재하는 수련반 이름입니다");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
