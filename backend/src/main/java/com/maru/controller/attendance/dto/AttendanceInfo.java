package com.maru.controller.attendance.dto;

import com.maru.domain.attendance.AttendanceStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record AttendanceInfo(
        String id,
        AttendanceStatus status,
        LocalDateTime checkinAt,
        LocalDateTime checkoutAt
) {
}
