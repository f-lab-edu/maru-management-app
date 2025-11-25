package com.maru.controller.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record OnboardingProfileReq(
        @NotBlank(message = "이름은 필수입니다")
        @Size(min = 2, max = 50, message = "이름은 2~50자 사이여야 합니다")
        String name,

        @Email(message = "유효한 이메일 형식이어야 합니다")
        String email,

        @Pattern(regexp = "^01[0-9]-\\d{3,4}-\\d{4}$", message = "010-XXXX-XXXX 형식으로 입력해주세요")
        String phone
) {}
