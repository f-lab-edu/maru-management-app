package com.maru.controller.attendance.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record BulkCheckRes(
        int successCount,
        int failureCount,
        List<AttendanceRes> successList,
        List<BulkCheckFailure> failureList
) {
}
