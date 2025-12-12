package com.maru.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "cookie")
public record CookieProperties(
        boolean secure,
        boolean httpOnly,
        String path,
        Duration maxAge,
        String sameSite
) {
}
