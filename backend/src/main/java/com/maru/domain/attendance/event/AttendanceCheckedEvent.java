package com.maru.domain.attendance.event;

/**
 * 출석/하원 체크 완료 이벤트
 *
 * @param attendanceId 출석 기록 ID
 * @param studentId 원생 ID
 * @param tenantId 테넌트 ID
 * @param isCheckin 출석 체크 여부 (true: 출석, false: 하원)
 */
public record AttendanceCheckedEvent(
        String attendanceId,
        String studentId,
        String tenantId,
        boolean isCheckin
) {
}
