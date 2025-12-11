package com.maru.controller.attendance.dto;

import lombok.Builder;

import java.time.LocalDate;
import java.util.Map;

@Builder
public record StudentAttendanceRow(
        Long id,
        String name,
        String photoUrl,
        String className,
        Map<LocalDate, AttendanceInfo> attendances
) {
}
