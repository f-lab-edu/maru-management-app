package com.maru.controller.student.dto;

import com.maru.domain.student.StudentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record StudentUpdateReq(
        @NotBlank String name,
        @NotNull LocalDate birth,
        String photoUrl,
        String phone,
        StudentStatus status,
        String statusChangeReason
) {}
