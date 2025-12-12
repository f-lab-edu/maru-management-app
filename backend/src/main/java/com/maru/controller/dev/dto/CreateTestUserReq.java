package com.maru.controller.dev.dto;

// TODO: 테스트용. 프로덕션에서는 반드시 삭제할 것.

import com.maru.domain.user.OnboardingStep;
import com.maru.domain.user.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateTestUserReq(
    @NotBlank String name,
    @NotBlank String phone,
    UserRole role,
    @NotNull OnboardingStep onboardingStep
) {}
