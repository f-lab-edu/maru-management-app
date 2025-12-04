package com.maru.controller.student.dto;

import com.maru.domain.student.StudentStatus;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record StudentRes(
        Long id,
        String name,
        LocalDate birth,
        String photoUrl,
        String phone,
        LocalDate enrolledAt,
        StudentStatus status,
        List<GuardianRes> guardians
) {}
