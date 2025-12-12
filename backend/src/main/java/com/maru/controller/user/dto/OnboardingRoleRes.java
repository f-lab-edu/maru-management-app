package com.maru.controller.user.dto;

import com.maru.domain.user.OnboardingStep;
import com.maru.domain.user.User;
import com.maru.domain.user.UserRole;
import lombok.Builder;

@Builder
public record OnboardingRoleRes(
        Long userId,
        UserRole role,
        OnboardingStep onboardingStep
) {
    public static OnboardingRoleRes from(User user) {
        return OnboardingRoleRes.builder()
                .userId(user.getId())
                .role(user.getRole())
                .onboardingStep(user.getOnboardingStep())
                .build();
    }
}
