package com.maru.service.auth.dto;

import lombok.Builder;

@Builder
public record TokenRes(
    String accessToken,
    String refreshToken,
    Long userId,
    String role
) {}
