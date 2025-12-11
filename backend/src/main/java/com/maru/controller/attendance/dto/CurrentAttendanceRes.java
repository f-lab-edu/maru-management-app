package com.maru.controller.attendance.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record CurrentAttendanceRes(
        int presentCount,
        int expectedCount,
        int currentlyPresent,
        List<AttendanceRes> attendanceList
) {
}
