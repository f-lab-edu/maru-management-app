package com.maru.controller.attendance.dto;

import lombok.Builder;

import java.time.LocalDate;
import java.util.Map;

@Builder
public record RangeAttendanceSummaryRes(
        int totalStudents,
        Map<LocalDate, DailySummaryRes> byDate
) {
}
