package com.maru.controller.auth.dto;

import lombok.Builder;

@Builder
public record TokenRes(
    String accessToken,
    String refreshToken,
    Long userId,
    Long tenantId,
    Long dojangId,
    String role
) {}
