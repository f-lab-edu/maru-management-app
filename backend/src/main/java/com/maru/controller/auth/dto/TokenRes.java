package com.maru.controller.auth.dto;

import lombok.Builder;

@Builder
public record TokenRes(
    String accessToken,
    String refreshToken,
    String userId,
    String tenantId,
    String dojangId,
    String role
) {}
