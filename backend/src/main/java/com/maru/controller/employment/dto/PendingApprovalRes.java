package com.maru.controller.employment.dto;

import com.maru.domain.employment.EmploymentStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record PendingApprovalRes(
        String id,
        String userId,
        String userName,
        String userEmail,
        String userPhone,
        LocalDateTime requestedAt,
        EmploymentStatus status
) {
}
