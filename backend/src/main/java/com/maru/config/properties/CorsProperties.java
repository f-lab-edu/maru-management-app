package com.maru.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "cors")
public record CorsProperties(
        String allowedOrigins,
        String allowedHeaders,
        String allowedMethods,
        Duration maxAge
) {
}
