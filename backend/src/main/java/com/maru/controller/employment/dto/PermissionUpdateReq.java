package com.maru.controller.employment.dto;

import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record PermissionUpdateReq(
        @NotNull(message = "권한 목록은 필수입니다")
        Set<String> permissions
) {
}
