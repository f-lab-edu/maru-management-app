package com.maru.controller.groups.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GroupUpdateReq(
        @NotBlank(message = "수련반 이름은 필수입니다")
        @Size(max = 50, message = "수련반 이름은 50자 이내여야 합니다")
        String name
) {}
