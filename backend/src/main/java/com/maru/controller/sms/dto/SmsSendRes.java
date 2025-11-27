package com.maru.controller.sms.dto;

import lombok.Builder;

@Builder
public record SmsSendRes(
    String phone,
    int expiresIn,
    String message
) {}
