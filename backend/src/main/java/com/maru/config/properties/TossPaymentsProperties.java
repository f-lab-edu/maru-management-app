package com.maru.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "toss.payments")
public record TossPaymentsProperties(
        String secretKey,
        String clientKey,
        String baseUrl,
        String encryptionKey
) {
}
