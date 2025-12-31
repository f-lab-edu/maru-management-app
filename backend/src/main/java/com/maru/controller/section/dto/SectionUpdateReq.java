package com.maru.controller.section.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SectionUpdateReq(
        @NotBlank(message = "수련부 이름은 필수입니다")
        @Size(max = 50, message = "수련부 이름은 50자 이내여야 합니다")
        String name
) {}
