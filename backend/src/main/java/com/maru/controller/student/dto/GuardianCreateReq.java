package com.maru.controller.student.dto;

import com.maru.domain.guardian.GuardianRelation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record GuardianCreateReq(
        @NotBlank String name,
        @NotBlank String phone,
        @NotNull GuardianRelation relation,
        boolean isPrimary
) {}
