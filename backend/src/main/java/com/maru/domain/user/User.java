package com.maru.domain.user;

import com.maru.common.exception.DomainAssert;
import com.maru.domain.common.SoftDeletableEntity;
import com.maru.domain.user.exception.UserErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "users",
    indexes = {
        @Index(name = "idx_user_email", columnList = "email"),
        @Index(name = "idx_user_phone", columnList = "phone"),
        @Index(name = "idx_user_role", columnList = "role")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends SoftDeletableEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String email;

    @Column(length = 20, unique = true)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OnboardingStep onboardingStep;

    @Column
    private LocalDateTime lastLoginAt;

    private User(String name, String email, String phone, UserRole role, OnboardingStep initialStep) {
        validateNotNull(name, initialStep);
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.onboardingStep = initialStep;
    }

    public static User createWithoutRole(String name, String email, String phone) {
        return new User(name, email, phone, null, OnboardingStep.PROFILE_INPUT);
    }

    public void selectOwnerRole() {
        this.role = UserRole.OWNER;
        this.onboardingStep = OnboardingStep.DOJANG_INFO;
    }

    public void selectInstructorRole() {
        this.role = UserRole.INSTRUCTOR;
        this.onboardingStep = OnboardingStep.APPROVAL_WAIT;
    }

    public void updateOnboardingStep(OnboardingStep step) {
        this.onboardingStep = step;
    }

    public void updateLastLoginAt() {
        this.lastLoginAt = LocalDateTime.now();
    }

    public void updateProfile(String name, String email, String phone) {
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    public void updateRole(UserRole role) {
        this.role = role;
    }

    private void validateNotNull(String name, OnboardingStep initialStep) {
        DomainAssert.hasText(name, UserErrorCode.NAME_REQUIRED);
        DomainAssert.notNull(initialStep, UserErrorCode.INITIAL_STEP_REQUIRED);
    }
}
