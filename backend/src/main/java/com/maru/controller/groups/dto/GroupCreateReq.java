package com.maru.controller.groups.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record GroupCreateReq(
        @NotNull(message = "수련부 선택은 필수입니다")
        String sectionId,

        @NotBlank(message = "수련반 이름은 필수입니다")
        @Size(max = 50, message = "수련반 이름은 50자 이내여야 합니다")
        String name
) {}
