package com.maru.domain.section.exception;

import com.maru.common.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SectionErrorCode implements BaseErrorCode {

    // 조회 실패
    NOT_FOUND(HttpStatus.NOT_FOUND, "SECTION_001", "수련부를 찾을 수 없습니다"),

    // 필수값 누락
    DOJANG_REQUIRED(HttpStatus.BAD_REQUEST, "SECTION_101", "도장 정보는 필수입니다"),
    NAME_REQUIRED(HttpStatus.BAD_REQUEST, "SECTION_102", "수련부 이름은 필수입니다"),

    // 비즈니스 규칙
    HAS_CLASSES(HttpStatus.CONFLICT, "SECTION_301", "소속 수련반이 있어 삭제할 수 없습니다"),
    DUPLICATE_NAME(HttpStatus.CONFLICT, "SECTION_302", "이미 존재하는 수련부 이름입니다"),
    REORDER_COUNT_MISMATCH(HttpStatus.BAD_REQUEST, "SECTION_303", "현재 수련부의 총 갯수와 변경하고자 하는 수련부의 갯수가 다릅니다"),
    DUPLICATE_ID_IN_REQUEST(HttpStatus.BAD_REQUEST, "SECTION_304", "중복된 수련부 ID가 포함되어 있습니다");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
