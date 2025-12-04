package com.maru.controller.student.dto;

import com.maru.domain.guardian.Guardian;
import com.maru.domain.guardian.GuardianRelation;
import com.maru.domain.guardian.Guardianship;
import lombok.Builder;

@Builder
public record GuardianRes(
        Long id,
        String name,
        String phone,
        GuardianRelation relation,
        boolean isPrimary,
        boolean isVerified
) {
    public static GuardianRes from(Guardianship guardianship) {
        Guardian guardian = guardianship.getGuardian();
        return GuardianRes.builder()
                .id(guardian.getId())
                .name(guardian.getName())
                .phone(guardian.getPhone())
                .relation(guardianship.getRelation())
                .isPrimary(guardianship.getIsPrimary())
                .isVerified(guardian.getIsVerified())
                .build();
    }
}
