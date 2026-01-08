package com.maru.controller.user.dto;

import com.maru.domain.user.UserRole;
import lombok.Builder;

@Builder
public record MyDojangRes(
        String dojangId,
        String dojangName,
        String tenantId,
        UserRole role
) {
}
