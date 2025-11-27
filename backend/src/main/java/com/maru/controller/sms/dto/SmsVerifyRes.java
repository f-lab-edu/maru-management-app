package com.maru.controller.sms.dto;

import lombok.Builder;

@Builder
public record SmsVerifyRes(
    boolean verified,
    Long userId,
    boolean isExistingUser,
    Integer remainingAttempts
) {}
