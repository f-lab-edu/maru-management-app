package com.maru.controller.user.dto;

import com.maru.domain.user.OnboardingStep;
import com.maru.domain.user.OAuthProvider;
import com.maru.domain.user.User;
import com.maru.domain.user.UserRole;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record UserMeRes(
        String id,
        String name,
        String email,
        String phone,
        // TODO : profile image user entity 에 나중에 추가할 것
        String profileImageUrl,
        UserRole role,
        OnboardingStep onboardingStep,
        OAuthProvider oauthProvider,
        LocalDateTime createdAt,
        LocalDateTime lastLoginAt
) {
    public static UserMeRes from(User user, OAuthProvider provider) {
        return UserMeRes.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .profileImageUrl(null)
                .role(user.getRole())
                .onboardingStep(user.getOnboardingStep())
                .oauthProvider(provider)
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }
}
