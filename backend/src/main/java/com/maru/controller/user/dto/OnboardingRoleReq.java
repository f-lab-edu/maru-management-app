package com.maru.controller.user.dto;

import com.maru.domain.user.UserRole;
import jakarta.validation.constraints.NotNull;

public record OnboardingRoleReq(
        @NotNull(message = "역할은 필수입니다")
        UserRole role
) {}
