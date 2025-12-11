package com.maru.controller.attendance.dto;

import lombok.Builder;

import java.time.LocalDate;
import java.util.Map;

@Builder
public record RangeAttendanceSummary(
        int totalStudents,
        Map<LocalDate, DailySummary> byDate
) {
}
