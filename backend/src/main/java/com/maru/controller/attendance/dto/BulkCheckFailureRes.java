package com.maru.controller.attendance.dto;

import lombok.Builder;

@Builder
public record BulkCheckFailureRes(
        Long studentId,
        String errorMessage
) {
}
