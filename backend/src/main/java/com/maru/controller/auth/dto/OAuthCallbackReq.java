package com.maru.controller.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record OAuthCallbackReq(
    @NotBlank(message = "인가 코드는 필수입니다")
    String code
) {}
