package com.maru.controller.user.dto;

import com.maru.domain.employment.Employment;
import com.maru.domain.user.UserRole;
import lombok.Builder;

@Builder
public record MyDojangRes(
        Long dojangId,
        String dojangName,
        Long tenantId,
        UserRole role
) {
    public static MyDojangRes from(Employment employment, Long userId) {
        return MyDojangRes.builder()
                .dojangId(employment.getDojang().getId())
                .dojangName(employment.getDojang().getName())
                .tenantId(employment.getTenant().getId())
                .role(employment.resolveRole(userId))
                .build();
    }
}
