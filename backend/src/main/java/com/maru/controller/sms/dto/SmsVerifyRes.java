package com.maru.controller.sms.dto;

import lombok.Builder;

@Builder
public record SmsVerifyRes(
    boolean verified,
    String userId,
    boolean isExistingUser
) {}
