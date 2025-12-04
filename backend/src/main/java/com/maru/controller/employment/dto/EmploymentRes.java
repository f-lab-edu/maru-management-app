package com.maru.controller.employment.dto;

import com.maru.domain.employment.Employment;
import com.maru.domain.employment.EmploymentStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record EmploymentRes(
        Long id,
        Long userId,
        Long dojangId,
        String dojangName,
        EmploymentStatus status,
        LocalDateTime requestedAt,
        LocalDateTime approvedAt,
        LocalDateTime rejectedAt
) {
    public static EmploymentRes from(Employment employment) {
        LocalDateTime approvedAt = null;
        LocalDateTime rejectedAt = null;

        if (employment.getStatus() == EmploymentStatus.ACTIVE) {
            approvedAt = employment.getJoinedAt();
        } else if (employment.getStatus() == EmploymentStatus.REJECTED) {
            rejectedAt = employment.getEndedAt();
        }

        return EmploymentRes.builder()
                .id(employment.getId())
                .userId(employment.getUser().getId())
                .dojangId(employment.getDojang().getId())
                .dojangName(employment.getDojang().getName())
                .status(employment.getStatus())
                .requestedAt(employment.getCreatedAt())
                .approvedAt(approvedAt)
                .rejectedAt(rejectedAt)
                .build();
    }
}
