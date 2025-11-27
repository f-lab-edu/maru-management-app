package com.maru.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sms.verification")
public record SmsVerificationProperties(
        int codeLength,
        int ttlMinutes,
        int resendLimitSeconds
) {
}
