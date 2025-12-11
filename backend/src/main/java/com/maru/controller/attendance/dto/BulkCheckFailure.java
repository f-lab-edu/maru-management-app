package com.maru.controller.attendance.dto;

import lombok.Builder;

@Builder
public record BulkCheckFailure(
        Long studentId,
        String errorMessage
) {
}
