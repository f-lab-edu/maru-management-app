package com.maru.controller.attendance.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record AttendanceTimeChangeReq(
        LocalDateTime checkinAt,
        LocalDateTime checkoutAt
) {
}
