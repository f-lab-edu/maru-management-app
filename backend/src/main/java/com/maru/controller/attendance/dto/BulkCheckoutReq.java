package com.maru.controller.attendance.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;

import java.util.List;

@Builder
public record BulkCheckoutReq(
        @NotEmpty(message = "출석 기록 ID 목록은 필수입니다") List<Long> attendanceIds
) {
}
