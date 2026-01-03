package com.maru.controller.attendance.dto;

import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record RangeAttendanceRes(
        LocalDate startDate,
        LocalDate endDate,
        int totalStudents,
        List<StudentAttendanceRowRes> students
) {
    public static RangeAttendanceRes empty(LocalDate startDate, LocalDate endDate) {
        return RangeAttendanceRes.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalStudents(0)
                .students(List.of())
                .build();
    }
}
