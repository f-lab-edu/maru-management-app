package com.maru.controller.employment.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record InstructorRes(
        String id,
        String userId,
        String name,
        String email,
        String phone,
        String status,
        LocalDateTime joinedAt,
        LocalDateTime suspendedAt,
        PermissionSummary permissionSummary
) {

    @Builder
    public record PermissionSummary(
            int total,
            int granted
    ) {
    }
}
