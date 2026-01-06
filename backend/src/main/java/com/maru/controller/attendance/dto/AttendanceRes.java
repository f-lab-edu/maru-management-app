package com.maru.controller.attendance.dto;

import com.maru.domain.attendance.AttendanceStatus;
import com.maru.domain.attendance.CheckMethod;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record AttendanceRes(
        String id,
        String studentId,
        String studentName,
        AttendanceStatus status,
        CheckMethod method,
        LocalDate attendanceDate,
        LocalDateTime checkinAt,
        LocalDateTime checkoutAt,
        String note,
        LocalDateTime createdAt
) {
}
