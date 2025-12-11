package com.maru.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DomainErrorCode {

    // Employment 관련
    EMPLOYMENT_NOT_PENDING("대기 상태의 고용만 처리할 수 있습니다"),
    EMPLOYMENT_NOT_ACTIVE("활성 상태의 고용만 정지할 수 있습니다"),
    EMPLOYMENT_NOT_SUSPENDED("정지 상태의 고용만 재활성화할 수 있습니다"),
    EMPLOYMENT_NOT_ACTIVE_OR_SUSPENDED("활성 또는 정지 상태의 고용만 퇴사 처리할 수 있습니다"),
    EMPLOYMENT_NOT_LEFT_OR_REJECTED("퇴사 또는 거절 상태의 고용만 재입사 처리할 수 있습니다"),
    EMPLOYMENT_TENANT_MISMATCH("도장의 Tenant와 입력된 Tenant가 일치하지 않습니다"),

    // Dojang 관련
    DOJANG_OWNER_TENANT_MISMATCH("도장 소유자는 테넌트 소유자와 일치해야 합니다"),

    // Attendance 관련
    ATTENDANCE_ALREADY_CHECKOUT("이미 퇴관 처리되었습니다"),
    ATTENDANCE_CHECKOUT_BEFORE_CHECKIN("퇴관 시각은 입관 시각보다 이후여야 합니다"),
    ATTENDANCE_SAME_STATUS("이미 동일한 상태입니다");

    private final String message;
}
