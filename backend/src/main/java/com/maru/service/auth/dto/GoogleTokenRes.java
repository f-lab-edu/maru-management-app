package com.maru.service.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GoogleTokenRes(
    @JsonProperty("access_token") String accessToken,
    @JsonProperty("token_type") String tokenType,
    @JsonProperty("expires_in") Integer expiresIn,
    @JsonProperty("refresh_token") String refreshToken
) {}
