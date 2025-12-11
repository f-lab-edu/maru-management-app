package com.maru.controller.attendance.dto;

import lombok.Builder;

@Builder
public record AttendanceStatsRes(
        int totalDays,
        int presentCount,
        int absentCount,
        int sickCount,
        int excusedCount,
        double attendanceRate
) {
}
