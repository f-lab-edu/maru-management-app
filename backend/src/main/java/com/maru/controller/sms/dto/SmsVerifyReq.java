package com.maru.controller.sms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SmsVerifyReq(
    @NotBlank(message = "전화번호는 필수입니다")
    String phone,

    @NotBlank(message = "인증번호는 필수입니다")
    @Size(min = 6, max = 6, message = "인증번호는 6자리입니다")
    String code
) {}
