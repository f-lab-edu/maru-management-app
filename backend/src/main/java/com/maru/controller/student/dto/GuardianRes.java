package com.maru.controller.student.dto;

import com.maru.domain.guardian.GuardianRelation;
import lombok.Builder;

@Builder
public record GuardianRes(
        String id,
        String name,
        String phone,
        GuardianRelation relation,
        boolean isPrimary,
        boolean isVerified
) {}
