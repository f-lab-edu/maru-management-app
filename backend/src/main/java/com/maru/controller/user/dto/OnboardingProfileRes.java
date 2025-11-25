package com.maru.controller.user.dto;

import com.maru.domain.user.OnboardingStep;
import com.maru.domain.user.User;
import lombok.Builder;

@Builder
public record OnboardingProfileRes(
        Long userId,
        String name,
        String email,
        String phone,
        OnboardingStep onboardingStep
) {
    public static OnboardingProfileRes from(User user) {
        return OnboardingProfileRes.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .onboardingStep(user.getOnboardingStep())
                .build();
    }
}
