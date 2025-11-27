package com.maru.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sms.solapi")
public record SmsProperties(
        String apiKey,
        String apiSecret,
        String senderNumber
) {
}
