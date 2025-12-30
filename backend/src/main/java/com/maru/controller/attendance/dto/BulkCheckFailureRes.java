package com.maru.controller.attendance.dto;

import lombok.Builder;

@Builder
public record BulkCheckFailureRes(
        String studentId,
        String errorMessage
) {
}
