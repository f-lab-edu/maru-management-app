package com.maru.controller.attendance.dto;

import com.maru.domain.attendance.Attendance;
import com.maru.domain.attendance.AttendanceStatus;
import com.maru.domain.attendance.CheckMethod;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record AttendanceRes(
        Long id,
        Long studentId,
        String studentName,
        AttendanceStatus status,
        CheckMethod method,
        LocalDate attendanceDate,
        LocalDateTime checkinAt,
        LocalDateTime checkoutAt,
        String note,
        LocalDateTime createdAt
) {
    public static AttendanceRes from(Attendance attendance) {
        return AttendanceRes.builder()
                .id(attendance.getId())
                .studentId(attendance.getStudent().getId())
                .studentName(attendance.getStudent().getName())
                .status(attendance.getStatus())
                .method(attendance.getMethod())
                .attendanceDate(attendance.getAttendanceDate())
                .checkinAt(attendance.getCheckinAt())
                .checkoutAt(attendance.getCheckoutAt())
                .note(attendance.getNote())
                .createdAt(attendance.getCreatedAt())
                .build();
    }
}
