package com.maru.controller.employment.dto;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Set;

@Builder
public record InstructorDetailRes(
        String id,
        String userId,
        String name,
        String email,
        String phone,
        String status,
        LocalDateTime joinedAt,
        LocalDateTime suspendedAt,
        Set<String> permissions
) {
}
