package com.maru.controller.attendance.dto;

import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record RangeAttendanceRes(
        LocalDate startDate,
        LocalDate endDate,
        List<LocalDate> dates,
        List<StudentAttendanceRow> students,
        RangeAttendanceSummary summary
) {
}
