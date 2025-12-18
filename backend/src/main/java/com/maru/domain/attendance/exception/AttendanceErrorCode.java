package com.maru.domain.attendance.exception;

import com.maru.common.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AttendanceErrorCode implements BaseErrorCode {

    // 조회 실패
    NOT_FOUND(HttpStatus.NOT_FOUND, "ATTENDANCE_001", "출석 기록을 찾을 수 없습니다"),

    // 필수값 누락
    STUDENT_REQUIRED(HttpStatus.BAD_REQUEST, "ATTENDANCE_101", "student는 필수입니다"),
    METHOD_REQUIRED(HttpStatus.BAD_REQUEST, "ATTENDANCE_102", "method는 필수입니다"),
    DATE_REQUIRED(HttpStatus.BAD_REQUEST, "ATTENDANCE_103", "date는 필수입니다"),
    CHECKIN_AT_REQUIRED(HttpStatus.BAD_REQUEST, "ATTENDANCE_104", "checkinAt은 필수입니다"),
    STATUS_REQUIRED(HttpStatus.BAD_REQUEST, "ATTENDANCE_105", "status는 필수입니다"),
    CHECKOUT_AT_REQUIRED(HttpStatus.BAD_REQUEST, "ATTENDANCE_106", "checkoutAt은 필수입니다"),

    // 유효성 검증
    STATUS_INVALID(HttpStatus.BAD_REQUEST, "ATTENDANCE_201", "유효하지 않은 출석 상태입니다"),

    // 비즈니스 규칙
    DUPLICATE(HttpStatus.CONFLICT, "ATTENDANCE_301", "이미 출석 체크되었습니다"),
    ALREADY_CHECKOUT(HttpStatus.BAD_REQUEST, "ATTENDANCE_302", "이미 퇴관 처리되었습니다"),
    NOT_CHECKOUT(HttpStatus.BAD_REQUEST, "ATTENDANCE_303", "퇴관 기록이 없습니다"),
    CHECKOUT_BEFORE_CHECKIN(HttpStatus.BAD_REQUEST, "ATTENDANCE_304", "퇴관 시각은 입관 시각보다 이후여야 합니다"),
    SAME_STATUS(HttpStatus.BAD_REQUEST, "ATTENDANCE_305", "이미 동일한 상태입니다"),
    DATE_RANGE_TOO_LARGE(HttpStatus.BAD_REQUEST, "ATTENDANCE_306", "조회 기간은 최대 31일입니다"),
    DATE_RANGE_INVALID(HttpStatus.BAD_REQUEST, "ATTENDANCE_307", "시작일은 종료일보다 이전이어야 합니다"),
    RETROACTIVE_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "ATTENDANCE_308", "소급 입력은 최대 30일까지 가능합니다");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
