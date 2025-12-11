package com.maru.controller.attendance.dto;

import com.maru.domain.attendance.AttendanceStatus;
import com.maru.domain.attendance.CheckMethod;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record AttendanceRes(
        Long id,
        Long studentId,
        String studentName,
        AttendanceStatus status,
        CheckMethod method,
        LocalDateTime checkinAt,
        LocalDateTime checkoutAt,
        String note,
        LocalDateTime createdAt
) {
}
