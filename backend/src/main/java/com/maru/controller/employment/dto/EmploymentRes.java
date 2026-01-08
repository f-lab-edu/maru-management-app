package com.maru.controller.employment.dto;

import com.maru.domain.employment.EmploymentStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record EmploymentRes(
        String id,
        String userId,
        String dojangId,
        String dojangName,
        EmploymentStatus status,
        LocalDateTime requestedAt,
        LocalDateTime approvedAt,
        LocalDateTime rejectedAt
) {
}
