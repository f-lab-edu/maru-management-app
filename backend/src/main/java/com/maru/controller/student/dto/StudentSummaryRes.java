package com.maru.controller.student.dto;

import com.maru.domain.student.StudentStatus;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record StudentSummaryRes(
        Long id,
        String name,
        LocalDate birth,
        String photoUrl,
        LocalDate enrolledAt,
        StudentStatus status
) {}
