package com.maru.controller.employment.dto;

import com.maru.domain.employment.Employment;
import com.maru.domain.employment.EmploymentStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record PendingApprovalRes(
        Long id,
        Long userId,
        String userName,
        String userEmail,
        String userPhone,
        LocalDateTime requestedAt,
        EmploymentStatus status
) {
    public static PendingApprovalRes from(Employment employment) {
        return PendingApprovalRes.builder()
                .id(employment.getId())
                .userId(employment.getUser().getId())
                .userName(employment.getUser().getName())
                .userEmail(employment.getUser().getEmail())
                .userPhone(employment.getUser().getPhone())
                .requestedAt(employment.getCreatedAt())
                .status(employment.getStatus())
                .build();
    }
}
