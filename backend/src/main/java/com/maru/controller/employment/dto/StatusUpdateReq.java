package com.maru.controller.employment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record StatusUpdateReq(
        @NotBlank(message = "상태는 필수입니다")
        @Pattern(regexp = "ACTIVE|SUSPENDED", message = "상태는 ACTIVE 또는 SUSPENDED만 가능합니다")
        String status,

        String reason
) {
}
