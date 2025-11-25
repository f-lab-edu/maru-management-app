package com.maru.controller.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record OnboardingDojangReq(
        @NotBlank(message = "도장 이름은 필수입니다")
        @Size(min = 2, max = 100, message = "도장 이름은 2~100자 사이여야 합니다")
        String name,

        @NotBlank(message = "주소는 필수입니다")
        String address,

        @NotBlank(message = "전화번호는 필수입니다")
        String phone
) {}
