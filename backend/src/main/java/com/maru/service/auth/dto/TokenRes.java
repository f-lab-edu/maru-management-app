package com.maru.service.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class TokenRes {
    private String accessToken;
    private String refreshToken;
    private Long userId;
    private String role;
}
