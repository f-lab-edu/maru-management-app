package com.maru.controller.user.dto;

import com.maru.domain.tenant.Dojang;
import com.maru.domain.user.OnboardingStep;
import com.maru.domain.user.User;
import lombok.Builder;

@Builder
public record OnboardingDojangRes(
        String userId,
        String tenantId,
        DojangInfo dojang,
        OnboardingStep onboardingStep
) {
    @Builder
    public record DojangInfo(
            String id,
            String name,
            String address,
            String phone
    ) {
        public static DojangInfo from(Dojang dojang) {
            return DojangInfo.builder()
                    .id(dojang.getId())
                    .name(dojang.getName())
                    .address(dojang.getAddress())
                    .phone(dojang.getPhone())
                    .build();
        }
    }

    public static OnboardingDojangRes from(User user, Dojang dojang) {
        return OnboardingDojangRes.builder()
                .userId(user.getId())
                .tenantId(dojang.getTenant().getId())
                .dojang(DojangInfo.from(dojang))
                .onboardingStep(user.getOnboardingStep())
                .build();
    }
}
