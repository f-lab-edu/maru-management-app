package com.maru.controller.sms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SmsSendReq(
    @NotBlank(message = "전화번호는 필수입니다")
    @Pattern(regexp = "010-\\d{4}-\\d{4}", message = "010-XXXX-XXXX 형식으로 입력해주세요")
    String phone
) {}
