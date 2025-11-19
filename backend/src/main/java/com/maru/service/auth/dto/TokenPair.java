package com.maru.service.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 토큰 쌍 (Access Token + Refresh Token)
 */
@Getter
@AllArgsConstructor
public class TokenPair {
    private String accessToken;
    private String refreshToken;
    private Long userId;
    private String role;
}
