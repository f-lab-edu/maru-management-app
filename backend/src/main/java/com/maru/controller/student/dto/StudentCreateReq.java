package com.maru.controller.student.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record StudentCreateReq(
        @NotBlank String name,
        @NotNull LocalDate birth,
        String photoUrl,
        String phone,
        List<GuardianCreateReq> guardians
) {
    public StudentCreateReq {
        if (guardians == null) guardians = List.of();
    }
}
